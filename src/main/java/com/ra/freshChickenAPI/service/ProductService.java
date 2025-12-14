package com.ra.freshChickenAPI.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ra.freshChickenAPI.entity.Product;
import com.ra.freshChickenAPI.repository.ProductRepository;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    public List<Product> getAvailableProducts() {
        return productRepository.findByAvailableTrue();
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndAvailableTrue(category);
    }
    
    // New methods for sale types
    public List<Product> getProductsBySaleType(String saleType) {
        return productRepository.findBySaleTypeAndAvailableTrue(saleType);
    }
    
    public List<Product> getRegularProducts() {
        return productRepository.findBySaleTypeIsNullAndAvailableTrue();
    }
    
    public Product createProduct(Product product, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }
            
            String fileName = fileStorageService.storeFile(image);
            product.setImagePath(fileName);
        }
        
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product productDetails, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setUnit(productDetails.getUnit());
        product.setAvailable(productDetails.getAvailable());
        product.setSaleType(productDetails.getSaleType());
        product.setDiscountPercentage(productDetails.getDiscountPercentage());
        product.setOriginalPrice(productDetails.getOriginalPrice());
        
        if (image != null && !image.isEmpty()) {
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }
            
            if (product.getImagePath() != null) {
                fileStorageService.deleteFile(product.getImagePath());
            }
            
            String fileName = fileStorageService.storeFile(image);
            product.setImagePath(fileName);
        }
        
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (product.getImagePath() != null) {
            fileStorageService.deleteFile(product.getImagePath());
        }
        
        productRepository.delete(product);
    }
    
    public Product updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setStockQuantity(quantity);
        return productRepository.save(product);
    }
}