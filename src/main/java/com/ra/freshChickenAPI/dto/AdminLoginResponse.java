package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private boolean success;
    private String message;
    private String token;
    private AdminDto admin;
}