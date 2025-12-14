package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {
    private String period;
    private BigDecimal totalSales;
    private BigDecimal totalRevenue;
    private Integer orderCount;
    private List<TopProductDto> topProducts;
    private List<DailySalesDto> dailyBreakdown;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TopProductDto {
    private Long productId;
    private String productName;
    private Integer quantitySold;
    private BigDecimal revenue;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class DailySalesDto {
    private String date;
    private BigDecimal sales;
    private BigDecimal revenue;
}