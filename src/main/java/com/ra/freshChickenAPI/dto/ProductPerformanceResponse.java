package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPerformanceResponse {
    private List<ProductPerformanceDto> products;
    private String period;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ProductPerformanceDto {
    private Long productId;
    private String productName;
    private Integer totalSold;
    private BigDecimal totalRevenue;
    private Integer stockRemaining;
    private Double sellThroughRate;
}