package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Long customerId;
    private Long addressId;
    private String deliverySlot;
    private String paymentMethod;
    private String specialInstructions;
    private List<OrderItemRequest> orderItems;
}
