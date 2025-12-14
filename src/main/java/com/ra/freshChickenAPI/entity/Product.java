package com.ra.freshChickenAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private String category; // e.g., "Chicken", "Country Chicken", "Egg"
    
    private Integer stockQuantity;
    
    private String unit; // e.g., "kg", "piece"
    
    private String imagePath;
    
    @Column(nullable = false)
    private Boolean available = true;
    
    // New field for sale type
    private String saleType; // e.g., "FLASH_SALE", "DIWALI_SALE", "FESTIVAL_SALE", null (for regular products)
    
    // Optional: Discount percentage for sale items
    private Integer discountPercentage;
    
    // Optional: Original price before discount
    private BigDecimal originalPrice;
}