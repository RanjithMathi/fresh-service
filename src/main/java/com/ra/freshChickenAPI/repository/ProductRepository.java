package com.ra.freshChickenAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ra.freshChickenAPI.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByAvailableTrue();
    List<Product> findByCategoryAndAvailableTrue(String category);
    
    // New methods for sale types
    List<Product> findBySaleTypeAndAvailableTrue(String saleType);
    List<Product> findBySaleType(String saleType);
    List<Product> findBySaleTypeIsNullAndAvailableTrue(); // Regular products (no sale)
}