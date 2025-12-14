package com.ra.freshChickenAPI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Stock quantity is required")
    private Integer stockQuantity;

    private String unit;

    private String imageUrl;

    @NotNull(message = "Available status is required")
    private Boolean isAvailable;

    private String saleType;

    private Integer discountPercentage;

    private BigDecimal originalPrice;
}