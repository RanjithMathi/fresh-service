package com.ra.freshChickenAPI.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ra.freshChickenAPI.dto.*;
import com.ra.freshChickenAPI.entity.Order;
import com.ra.freshChickenAPI.entity.OrderItem;
import com.ra.freshChickenAPI.entity.Product;
import com.ra.freshChickenAPI.repository.OrderRepository;
import com.ra.freshChickenAPI.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// Inner classes for DTOs
class TopProductDto {
    private Long productId;
    private String productName;
    private Integer quantitySold;
    private BigDecimal revenue;

    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantitySold() { return quantitySold; }
    public void setQuantitySold(Integer quantitySold) { this.quantitySold = quantitySold; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}

class DailySalesDto {
    private String date;
    private BigDecimal sales;
    private BigDecimal revenue;

    // Getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public BigDecimal getSales() { return sales; }
    public void setSales(BigDecimal sales) { this.sales = sales; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}

class ProductPerformanceDto {
    private Long productId;
    private String productName;
    private Integer totalSold;
    private BigDecimal totalRevenue;
    private Integer stockRemaining;
    private Double sellThroughRate;

    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getTotalSold() { return totalSold; }
    public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Integer getStockRemaining() { return stockRemaining; }
    public void setStockRemaining(Integer stockRemaining) { this.stockRemaining = stockRemaining; }

    public Double getSellThroughRate() { return sellThroughRate; }
    public void setSellThroughRate(Double sellThroughRate) { this.sellThroughRate = sellThroughRate; }
}

@Service
public class ReportsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    public SalesReportResponse getSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate, null).getContent();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top products calculation
        Map<Long, TopProductDto> productSales = new HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                Long productId = item.getProduct().getId();
                productSales.computeIfAbsent(productId, id -> {
                    TopProductDto dto = new TopProductDto();
                    dto.setProductId(id);
                    dto.setProductName(item.getProduct().getName());
                    dto.setQuantitySold(0);
                    dto.setRevenue(BigDecimal.ZERO);
                    return dto;
                });
                TopProductDto dto = productSales.get(productId);
                dto.setQuantitySold(dto.getQuantitySold() + item.getQuantity());
                dto.setRevenue(dto.getRevenue().add(item.getSubtotal()));
            }
        }

        List<TopProductDto> topProducts = productSales.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(10)
                .collect(Collectors.toList());

        // Daily breakdown (simplified - you might want to group by date properly)
        List<DailySalesDto> dailyBreakdown = new ArrayList<>();
        // Implementation depends on your date grouping needs

        return new SalesReportResponse(
                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " to " +
                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                BigDecimal.valueOf(orders.size()), // total sales count
                totalRevenue,
                orders.size(),
                new ArrayList<>(), // topProducts - need to convert
                new ArrayList<>() // dailyBreakdown
        );
    }

    public RevenueReportResponse getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate, null).getContent();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = orders.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        // Calculate previous period for growth rate
        LocalDateTime previousStart = startDate.minusDays(endDate.toLocalDate().toEpochDay() - startDate.toLocalDate().toEpochDay() + 1);
        LocalDateTime previousEnd = startDate.minusDays(1);
        List<Order> previousOrders = orderRepository.findByOrderDateBetween(previousStart, previousEnd, null).getContent();
        BigDecimal previousRevenue = previousOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal growthRate = previousRevenue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                totalRevenue.subtract(previousRevenue).divide(previousRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return new RevenueReportResponse(
                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " to " +
                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                totalRevenue,
                averageOrderValue,
                orders.size(),
                growthRate
        );
    }

    public CustomerAnalyticsResponse getCustomerAnalytics() {
        List<Order> allOrders = orderRepository.findAll();
        List<Product> allCustomers = productRepository.findAll(); // This should be customer repository

        // This is a simplified implementation - you would need a CustomerRepository
        // For now, returning basic stats
        return new CustomerAnalyticsResponse(
                0, // totalCustomers
                0, // activeCustomers
                0, // newCustomersThisMonth
                BigDecimal.ZERO, // averageOrderValue
                0.0, // customerRetentionRate
                0 // repeatCustomers
        );
    }

    public ProductPerformanceResponse getProductPerformance(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate, null).getContent();
        List<Product> allProducts = productRepository.findAll();

        Map<Long, ProductPerformanceDto> productPerformance = new HashMap<>();

        // Initialize with all products
        for (Product product : allProducts) {
            ProductPerformanceDto dto = new ProductPerformanceDto();
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setTotalSold(0);
            dto.setTotalRevenue(BigDecimal.ZERO);
            dto.setStockRemaining(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
            productPerformance.put(product.getId(), dto);
        }

        // Calculate sales data
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                ProductPerformanceDto dto = productPerformance.get(item.getProduct().getId());
                if (dto != null) {
                    dto.setTotalSold(dto.getTotalSold() + item.getQuantity());
                    dto.setTotalRevenue(dto.getTotalRevenue().add(item.getSubtotal()));
                }
            }
        }

        // Calculate sell-through rate
        List<ProductPerformanceDto> products = productPerformance.values().stream()
                .peek(dto -> {
                    int initialStock = dto.getStockRemaining() + dto.getTotalSold();
                    dto.setSellThroughRate(initialStock == 0 ? 0.0 :
                            (double) dto.getTotalSold() / initialStock * 100);
                })
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .collect(Collectors.toList());

        return new ProductPerformanceResponse(
                new ArrayList<>(), // products - need to convert
                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " to " +
                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
    }
}