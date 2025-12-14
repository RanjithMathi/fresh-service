package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {
    private List<OrderDTO> orders;
    private int totalCount;
    private int page;
    private int pageSize;
    private int totalPages;
    private String status;
    private String dateFrom;
    private String dateTo;
}