package com.ra.freshChickenAPI.dto;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private String status;
    private String notes;
}