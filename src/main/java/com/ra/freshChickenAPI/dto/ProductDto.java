package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String categoryName;
    private Integer stockQuantity;
    private String unit;
    private String imageUrl;
    private Boolean isAvailable;
    private String saleType;
    private Integer discountPercentage;
    private BigDecimal originalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}