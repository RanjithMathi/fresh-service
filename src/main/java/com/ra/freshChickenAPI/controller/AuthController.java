package com.ra.freshChickenAPI.controller;

import com.ra.freshChickenAPI.dto.AuthResponse;
import com.ra.freshChickenAPI.dto.SendOtpRequest;
import com.ra.freshChickenAPI.dto.VerifyOtpRequest;
import com.ra.freshChickenAPI.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/send-otp")
    public ResponseEntity<AuthResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        AuthResponse response = authService.sendOtp(request.getPhoneNumber());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtpAndLogin(
            request.getPhoneNumber(),
            request.getOtp()
        );
        return ResponseEntity.ok(response);
    }
}