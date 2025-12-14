package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListResponse {
    private List<CustomerDto> customers;
    private int totalCount;
    private int page;
    private int pageSize;
    private int totalPages;
}