package com.ra.freshChickenAPI.dto;

import com.ra.freshChickenAPI.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    
    private Long id;
    
    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;
    
    private String addressLine2;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Invalid Indian PIN code")
    private String zipCode;
    
    private String landmark;
    
    private AddressType addressType;
    
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String contactPhone;
    
    private Boolean isDefault;
    
    private Long customerId; // For reference
}