package com.ra.freshChickenAPI.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ra.freshChickenAPI.dto.OrderActivityMessage;
import com.ra.freshChickenAPI.dto.OrderDTO;
import com.ra.freshChickenAPI.dto.OrderListResponse;
import com.ra.freshChickenAPI.dto.UpdateOrderStatusRequest;
import com.ra.freshChickenAPI.entity.Order;
import com.ra.freshChickenAPI.entity.OrderStatus;
import com.ra.freshChickenAPI.mapper.OrderMapper;
import com.ra.freshChickenAPI.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/orders")
@CrossOrigin(origins = "*")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping
    public ResponseEntity<OrderListResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderService.getAllOrders(pageable, status, dateFrom, dateTo);

        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());

        OrderListResponse response = new OrderListResponse(
                orderDTOs,
                (int) orderPage.getTotalElements(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalPages(),
                status != null ? status.toString() : null,
                dateFrom != null ? dateFrom.toString() : null,
                dateTo != null ? dateTo.toString() : null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(orderMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {

        try {
            OrderStatus status = OrderStatus.valueOf(request.getStatus().toUpperCase());
            Order updated = orderService.updateOrderStatus(id, status);

            // TODO: Add notes to order if needed (extend Order entity)

            OrderDTO orderDTO = orderMapper.toDTO(updated);
            return ResponseEntity.ok(orderDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/delivery-tracking")
    public ResponseEntity<List<OrderDTO>> getDeliveryTracking() {
        List<Order> orders = orderService.getOrdersByStatuses(
                List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED));

        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/real-time-activities")
    public ResponseEntity<String> getRealTimeActivitiesInfo() {
        return ResponseEntity.ok(
                "Real-time order activities are available via WebSocket at /ws endpoint. " +
                "Subscribe to /topic/order-activity for live updates on order creation and status changes."
        );
    }
}