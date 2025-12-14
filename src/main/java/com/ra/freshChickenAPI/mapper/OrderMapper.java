package com.ra.freshChickenAPI.mapper;

import com.ra.freshChickenAPI.dto.OrderDTO;
import com.ra.freshChickenAPI.dto.OrderItemDTO;
import com.ra.freshChickenAPI.entity.Order;
import com.ra.freshChickenAPI.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
        
        return OrderDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : null)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhone() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .specialInstructions(order.getSpecialInstructions())
                .addressId(order.getAddress() != null ? order.getAddress().getId() : null)
                .deliveryAddressLine1(order.getDeliveryAddressLine1())
                .deliveryAddressLine2(order.getDeliveryAddressLine2())
                .deliveryCity(order.getDeliveryCity())
                .deliveryState(order.getDeliveryState())
                .deliveryZipCode(order.getDeliveryZipCode())
                .deliveryLandmark(order.getDeliveryLandmark())
                .deliveryContactPhone(order.getDeliveryContactPhone())
                .fullDeliveryAddress(order.getFullDeliveryAddress())
                .orderItems(order.getOrderItems().stream()
                        .map(this::toOrderItemDTO)
                        .collect(Collectors.toList()))
                .build();
    }
    
    public OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
                .productName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : null)
                .productImage(orderItem.getProduct() != null ? orderItem.getProduct().getImagePath() : null)
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .subtotal(orderItem.getSubtotal())
                .build();
    }
}