package com.ra.freshChickenAPI.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ra.freshChickenAPI.dto.CreateProductRequest;
import com.ra.freshChickenAPI.dto.ProductDto;
import com.ra.freshChickenAPI.dto.ProductListResponse;
import com.ra.freshChickenAPI.entity.Product;
import com.ra.freshChickenAPI.service.ProductService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/products")
@CrossOrigin(origins = "*")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.getAllProducts(pageable);

        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        ProductListResponse response = new ProductListResponse(
                productDtos,
                (int) productPage.getTotalElements(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(convertToDto(product));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("unit") String unit,
            @RequestParam("isAvailable") Boolean isAvailable,
            @RequestParam(value = "saleType", required = false) String saleType,
            @RequestParam(value = "discountPercentage", required = false) Integer discountPercentage,
            @RequestParam(value = "originalPrice", required = false) java.math.BigDecimal originalPrice,
            @RequestParam("image") MultipartFile image) {

        try {
            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select an image file");
            }

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setCategory(category);
            product.setStockQuantity(stockQuantity);
            product.setUnit(unit);
            product.setAvailable(isAvailable);
            product.setSaleType(saleType);
            product.setDiscountPercentage(discountPercentage);
            product.setOriginalPrice(originalPrice);

            Product created = productService.createProduct(product, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("unit") String unit,
            @RequestParam("isAvailable") Boolean isAvailable,
            @RequestParam(value = "saleType", required = false) String saleType,
            @RequestParam(value = "discountPercentage", required = false) Integer discountPercentage,
            @RequestParam(value = "originalPrice", required = false) java.math.BigDecimal originalPrice,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            Product productDetails = new Product();
            productDetails.setName(name);
            productDetails.setDescription(description);
            productDetails.setPrice(price);
            productDetails.setCategory(category);
            productDetails.setStockQuantity(stockQuantity);
            productDetails.setUnit(unit);
            productDetails.setAvailable(isAvailable);
            productDetails.setSaleType(saleType);
            productDetails.setDiscountPercentage(discountPercentage);
            productDetails.setOriginalPrice(originalPrice);

            Product updated = productService.updateProduct(id, productDetails, image);
            return ResponseEntity.ok(convertToDto(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error updating product: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDto> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            Product updated = productService.updateStock(id, quantity);
            return ResponseEntity.ok(convertToDto(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setUnit(product.getUnit());
        dto.setImageUrl(product.getImagePath() != null ? "/api/products/images/" + product.getImagePath() : null);
        dto.setIsAvailable(product.getAvailable());
        dto.setSaleType(product.getSaleType());
        dto.setDiscountPercentage(product.getDiscountPercentage());
        dto.setOriginalPrice(product.getOriginalPrice());
        return dto;
    }
}