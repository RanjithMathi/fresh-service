package com.ra.freshChickenAPI.dto;

import com.ra.freshChickenAPI.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private String specialInstructions;
    
    // Address details
    private Long addressId;
    private String deliveryAddressLine1;
    private String deliveryAddressLine2;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryZipCode;
    private String deliveryLandmark;
    private String deliveryContactPhone;
    private String fullDeliveryAddress;
    
    // Order items
    private List<OrderItemDTO> orderItems;
}