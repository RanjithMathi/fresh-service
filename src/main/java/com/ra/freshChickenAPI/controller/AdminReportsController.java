package com.ra.freshChickenAPI.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ra.freshChickenAPI.dto.*;
import com.ra.freshChickenAPI.service.ReportsService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/reports")
@CrossOrigin(origins = "*")
public class AdminReportsController {

    @Autowired
    private ReportsService reportsService;

    @GetMapping("/sales")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        SalesReportResponse report = reportsService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportResponse> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        RevenueReportResponse report = reportsService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/customers")
    public ResponseEntity<CustomerAnalyticsResponse> getCustomerAnalytics() {
        CustomerAnalyticsResponse analytics = reportsService.getCustomerAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/products")
    public ResponseEntity<ProductPerformanceResponse> getProductPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        ProductPerformanceResponse performance = reportsService.getProductPerformance(startDate, endDate);
        return ResponseEntity.ok(performance);
    }
}