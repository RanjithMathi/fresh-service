package com.ra.freshChickenAPI.service;

import com.ra.freshChickenAPI.controller.OrderActivityController;
import com.ra.freshChickenAPI.dto.CreateOrderRequest;
import com.ra.freshChickenAPI.dto.OrderActivityMessage;
import com.ra.freshChickenAPI.dto.OrderItemRequest;
import com.ra.freshChickenAPI.entity.*;
import com.ra.freshChickenAPI.repository.AddressRepository;
import com.ra.freshChickenAPI.repository.CustomerRepository;
import com.ra.freshChickenAPI.repository.OrderRepository;
import com.ra.freshChickenAPI.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderActivityController orderActivityController;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Autowired
    private FCMTokenService fcmTokenService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> getAllOrders(Pageable pageable, OrderStatus status, LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (status != null && dateFrom != null && dateTo != null) {
            return orderRepository.findByStatusAndOrderDateBetween(status, dateFrom, dateTo, pageable);
        } else if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        } else if (dateFrom != null && dateTo != null) {
            return orderRepository.findByOrderDateBetween(dateFrom, dateTo, pageable);
        } else {
            return orderRepository.findAll(pageable);
        }
    }

    public List<Order> getOrdersByStatuses(List<OrderStatus> statuses) {
        return orderRepository.findByStatusIn(statuses);
    }
    
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    @Transactional
    public Order createOrderFromRequest(CreateOrderRequest request) {
        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));
        
        // Validate address
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + request.getAddressId()));
        
        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Address does not belong to this customer");
        }
        
        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        
        // Parse and set delivery date from deliverySlot if needed
        // Format: "2024-11-05 - 10:00 AM - 12:00 PM"
        if (request.getDeliverySlot() != null && !request.getDeliverySlot().isEmpty()) {
            // You can parse this and set deliveryDate if needed
            // For now, we'll store it in specialInstructions or create a new field
        }
        
        // Create order items and calculate total
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setSubtotal(subtotal);
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }
        
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Send real-time notification for new order
        OrderActivityMessage message = new OrderActivityMessage(
                "ORDER_CREATED",
                savedOrder.getId(),
                customer.getName(),
                savedOrder.getStatus().toString(),
                null,
                LocalDateTime.now(),
                "New order #" + savedOrder.getId() + " placed by " + customer.getName()
        );
        System.out.println("📡 Sending WebSocket notification for new order: " + message);
        orderActivityController.sendOrderActivityNotification(message);
        System.out.println("✅ WebSocket notification sent for order #" + savedOrder.getId());

        // Save persistent notification for admins
        notificationService.createNotification(
                "ORDER_CREATED",
                "New Order Received",
                "New order #" + savedOrder.getId() + " placed by " + customer.getName(),
                "{\"orderId\": " + savedOrder.getId() + ", \"customerName\": \"" + customer.getName() + "\", \"totalAmount\": " + savedOrder.getTotalAmount() + "}"
        );

        // Send FCM notifications to all admins
        try {
            List<String> adminTokens = fcmTokenService.getAllActiveAdminTokens();
            if (!adminTokens.isEmpty()) {
                firebaseMessagingService.sendOrderCreatedNotification(
                        adminTokens,
                        savedOrder.getId(),
                        customer.getName(),
                        savedOrder.getTotalAmount().toString()
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send FCM notification for order creation: " + e.getMessage());
            // Don't fail the order creation if FCM fails
        }

        return savedOrder;
    }
    

    
    @Transactional
    public Order createOrder(Order order) {
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        
        // Calculate total from order items
        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalAmount(total);
        
        // Set order reference for each order item
        order.getOrderItems().forEach(item -> {
            item.setOrder(order);
            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(subtotal);
        });
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus status) {
        logger.info("🔄 [ORDER-STATUS-UPDATE] Starting order status update for Order ID: {}", id);
        logger.info("🔄 [ORDER-STATUS-UPDATE] Requested new status: {}", status);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        String previousStatus = order.getStatus().toString();
        logger.info("📝 [ORDER-STATUS-UPDATE] Previous status: {}", previousStatus);
        logger.info("👤 [ORDER-STATUS-UPDATE] Customer ID: {}, Customer Name: {}",
                   order.getCustomer().getId(), order.getCustomer().getName());

        order.setStatus(status);

        if (status == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
            logger.info("📅 [ORDER-STATUS-UPDATE] Set delivery date to: {}", order.getDeliveryDate());
        }

        Order updatedOrder = orderRepository.save(order);
        logger.info("💾 [ORDER-STATUS-UPDATE] Order saved successfully with new status");

        // Send real-time notification for status update
        logger.info("📡 [ORDER-STATUS-UPDATE] Sending WebSocket notification...");
        OrderActivityMessage message = new OrderActivityMessage(
                "ORDER_STATUS_UPDATED",
                updatedOrder.getId(),
                updatedOrder.getCustomer().getName(),
                status.toString(),
                previousStatus,
                LocalDateTime.now(),
                "Order #" + updatedOrder.getId() + " status changed from " + previousStatus + " to " + status.toString()
        );
        orderActivityController.sendOrderActivityNotification(message);
        logger.info("✅ [ORDER-STATUS-UPDATE] WebSocket notification sent successfully");

        // Save persistent notification for admins
        logger.info("💾 [ORDER-STATUS-UPDATE] Creating persistent notification for admins...");
        notificationService.createNotification(
                "ORDER_STATUS_UPDATED",
                "Order Status Updated",
                "Order #" + updatedOrder.getId() + " status changed from " + previousStatus + " to " + status.toString(),
                "{\"orderId\": " + updatedOrder.getId() + ", \"customerName\": \"" + updatedOrder.getCustomer().getName() + "\", \"oldStatus\": \"" + previousStatus + "\", \"newStatus\": \"" + status.toString() + "\"}"
        );
        logger.info("✅ [ORDER-STATUS-UPDATE] Persistent notification created");

        // Send FCM notifications to all admins
		/*
		 * logger.info("📤 [ORDER-STATUS-UPDATE] Sending FCM notifications to admins..."
		 * ); try { List<String> adminTokens =
		 * fcmTokenService.getAllActiveAdminTokens();
		 * logger.info("📱 [ORDER-STATUS-UPDATE] Found {} active admin tokens",
		 * adminTokens.size());
		 * 
		 * if (!adminTokens.isEmpty()) {
		 * firebaseMessagingService.sendOrderStatusUpdatedNotification( adminTokens,
		 * updatedOrder.getId(), updatedOrder.getCustomer().getName(), previousStatus,
		 * status.toString() ); logger.
		 * info("✅ [ORDER-STATUS-UPDATE] Admin FCM notification sent successfully"); }
		 * else { logger.warn("⚠️ [ORDER-STATUS-UPDATE] No active admin tokens found");
		 * } } catch (Exception e) { logger.
		 * error("❌ [ORDER-STATUS-UPDATE] Failed to send admin FCM notification: {}",
		 * e.getMessage()); // Don't fail the status update if FCM fails }
		 */

        // Send customer notification if status actually changed
        if (!previousStatus.equals(status.toString())) {
            logger.info("👤 [ORDER-STATUS-UPDATE] Status changed, sending customer notification...");
            try {
                List<String> customerTokens = fcmTokenService.getActiveTokensForCustomer(updatedOrder.getCustomer().getId());
                logger.info("📱 [ORDER-STATUS-UPDATE] Found {} active customer tokens for customer ID: {}",
                           customerTokens.size(), updatedOrder.getCustomer().getId());
                
                if (!customerTokens.isEmpty()) {
                    firebaseMessagingService.sendCustomerOrderStatusNotification(
                            customerTokens,
                            updatedOrder.getId(),
                            previousStatus,
                            status.toString()
                    );
                    logger.info("✅ [ORDER-STATUS-UPDATE] Customer notification sent successfully for order #" + updatedOrder.getId());
                } else {
                    logger.warn("⚠️ [ORDER-STATUS-UPDATE] No active customer tokens found for customer ID: {}",
                               updatedOrder.getCustomer().getId());
                }
            } catch (Exception e) {
                logger.error("❌ [ORDER-STATUS-UPDATE] Failed to send customer FCM notification: {}", e.getMessage());
                // Don't fail the status update if customer FCM fails
            }
        } else {
            logger.info("ℹ️ [ORDER-STATUS-UPDATE] No status change detected, skipping customer notification");
        }

        logger.info("✅ [ORDER-STATUS-UPDATE] Order status update completed successfully for Order ID: {}", id);
        logger.info("📊 [ORDER-STATUS-UPDATE] Summary - Previous: {}, New: {}, Customer: {}",
                   previousStatus, status, updatedOrder.getCustomer().getName());
        
        return updatedOrder;
    }
    
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}