package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderActivityMessage {
    private String type; // "ORDER_CREATED", "ORDER_STATUS_UPDATED", "ORDER_DELIVERED"
    private Long orderId;
    private String customerName;
    private String status;
    private String previousStatus;
    private LocalDateTime timestamp;
    private String message;
}