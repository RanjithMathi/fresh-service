package com.ra.freshChickenAPI.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ra.freshChickenAPI.dto.CustomerDto;
import com.ra.freshChickenAPI.dto.CustomerListResponse;
import com.ra.freshChickenAPI.dto.OrderDTO;
import com.ra.freshChickenAPI.entity.Customer;
import com.ra.freshChickenAPI.mapper.OrderMapper;
import com.ra.freshChickenAPI.service.CustomerService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/customers")
@CrossOrigin(origins = "*")
public class AdminCustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping
    public ResponseEntity<CustomerListResponse> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = customerService.getAllCustomers(pageable);

        List<CustomerDto> customerDtos = customerPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        CustomerListResponse response = new CustomerListResponse(
                customerDtos,
                (int) customerPage.getTotalElements(),
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(customer -> ResponseEntity.ok(convertToDto(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @RequestBody CustomerDto customerDto) {
        try {
            Customer customerDetails = new Customer();
            customerDetails.setName(customerDto.getName());
            customerDetails.setEmail(customerDto.getEmail());
            customerDetails.setPhone(customerDto.getPhone());
            customerDetails.setIsActive(customerDto.getIsActive());

            Customer updated = customerService.updateCustomer(id, customerDetails);
            return ResponseEntity.ok(convertToDto(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderDTO>> getCustomerOrders(@PathVariable Long id) {
        List<com.ra.freshChickenAPI.entity.Order> orders = customerService.getCustomerOrders(id);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    private CustomerDto convertToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setIsActive(customer.getIsActive());
        dto.setCreatedAt(customer.getCreatedAt());

        // Calculate total spent and orders from orders
        List<com.ra.freshChickenAPI.entity.Order> orders = customerService.getCustomerOrders(customer.getId());
        dto.setTotalOrders(orders.size());
        dto.setTotalSpent(orders.stream()
                .map(com.ra.freshChickenAPI.entity.Order::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

        if (!orders.isEmpty()) {
            dto.setLastOrderDate(orders.stream()
                    .map(com.ra.freshChickenAPI.entity.Order::getOrderDate)
                    .max(java.time.LocalDateTime::compareTo)
                    .orElse(null));
        }

        return dto;
    }
}