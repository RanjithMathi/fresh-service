package com.ra.freshChickenAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String phoneNumber;
    private String name;
    private String email;
    
    // DEPRECATED: Use addresses list instead
    @Deprecated
    private String address;
    
    // NEW: List of addresses
    private List<AddressDto> addresses;
    
    // Helper flag to indicate if user has any addresses
    private boolean hasAddresses;
}