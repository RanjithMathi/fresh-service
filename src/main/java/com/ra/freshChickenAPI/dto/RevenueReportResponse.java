package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    private String period;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Integer totalOrders;
    private BigDecimal growthRate; // compared to previous period
}