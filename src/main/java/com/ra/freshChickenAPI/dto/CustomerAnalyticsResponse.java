package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsResponse {
    private Integer totalCustomers;
    private Integer activeCustomers;
    private Integer newCustomersThisMonth;
    private BigDecimal averageOrderValue;
    private Double customerRetentionRate;
    private Integer repeatCustomers;
}