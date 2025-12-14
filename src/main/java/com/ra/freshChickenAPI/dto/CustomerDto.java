package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private List<AddressDto> addresses;
    private BigDecimal totalSpent;
    private Integer totalOrders;
    private LocalDateTime lastOrderDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
}