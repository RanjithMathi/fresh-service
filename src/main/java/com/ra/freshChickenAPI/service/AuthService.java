package com.ra.freshChickenAPI.service;

import com.ra.freshChickenAPI.dto.AddressDto;
import com.ra.freshChickenAPI.dto.AuthResponse;
import com.ra.freshChickenAPI.dto.UserDto;
import com.ra.freshChickenAPI.entity.Address;
import com.ra.freshChickenAPI.entity.Customer;
import com.ra.freshChickenAPI.repository.AddressRepository;
import com.ra.freshChickenAPI.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final CustomerRepository userRepository;
    private final OtpService otpService;
    private final AddressRepository addressRepository;
    
    @Transactional
    public AuthResponse sendOtp(String phoneNumber) {
        try {
            boolean otpSent = otpService.generateAndSendOtp(phoneNumber);
            
            if (otpSent) {
                return new AuthResponse(
                    true,
                    "OTP sent successfully to " + phoneNumber,
                    null,
                    false,
                    null
                );
            } else {
                return new AuthResponse(
                    false,
                    "Failed to send OTP. Please try again.",
                    null,
                    false,
                    null
                );
            }
        } catch (Exception e) {
            log.error("Error sending OTP: {}", e.getMessage());
            return new AuthResponse(
                false,
                "An error occurred. Please try again.",
                null,
                false,
                null
            );
        }
    }
    
    @Transactional
    public AuthResponse verifyOtpAndLogin(String phoneNumber, String otp) {
        try {
            // Verify OTP
            boolean isValid = otpService.verifyOtp(phoneNumber, otp);
            
            if (!isValid) { 
                return new AuthResponse(
                    false,
                    "Invalid or expired OTP",
                    null,
                    false,
                    null
                );
            }
            
            // Find or create user
            Customer user = userRepository.findByPhone(phoneNumber)
                .orElseGet(() -> createNewUser(phoneNumber));
            
            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Get user's addresses
            List<Address> addresses = addressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
            
            // Convert addresses to DTOs
            List<AddressDto> addressDtos = addresses.stream()
                .map(this::convertToAddressDto)
                .collect(Collectors.toList());
            
            // Convert to DTO
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setPhoneNumber(user.getPhone());
            userDto.setName(user.getName());
            userDto.setEmail(user.getEmail());
            userDto.setAddress(user.getAddress()); // Deprecated field for backward compatibility
            userDto.setAddresses(addressDtos);
            userDto.setHasAddresses(!addresses.isEmpty());
            
            // TODO: Generate JWT token here for production
            String token = "temp_token_" + user.getId(); // Replace with actual JWT
            
            return new AuthResponse(
                true,
                "Login successful",
                userDto,
                !addresses.isEmpty(),
                token
            );
            
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage());
            return new AuthResponse(
                false,
                "An error occurred during login",
                null,
                false,
                null
            );
        }
    }
    
    private Customer createNewUser(String phoneNumber) {
        Customer user = new Customer();
        user.setPhone(phoneNumber);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());
        
        log.info("Creating new user with phone number: {}", phoneNumber);
        return userRepository.save(user);
    }
    
    private AddressDto convertToAddressDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setLandmark(address.getLandmark());
        dto.setAddressType(address.getAddressType());
        dto.setContactPhone(address.getContactPhone());
        dto.setIsDefault(address.getIsDefault());
        dto.setCustomerId(address.getCustomer().getId());
        return dto;
    }
}