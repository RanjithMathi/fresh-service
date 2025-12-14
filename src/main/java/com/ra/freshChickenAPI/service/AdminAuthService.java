package com.ra.freshChickenAPI.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ra.freshChickenAPI.dto.AdminDto;
import com.ra.freshChickenAPI.dto.AdminLoginRequest;
import com.ra.freshChickenAPI.dto.AdminLoginResponse;
import com.ra.freshChickenAPI.entity.Admin;
import com.ra.freshChickenAPI.repository.AdminRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminAuthService {

    @Autowired
    private AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminLoginResponse login(AdminLoginRequest request) {
        try {
            Optional<Admin> adminOpt = adminRepository.findByUsername(request.getUsername());

            if (adminOpt.isEmpty()) {
                return new AdminLoginResponse(false, "Invalid username or password", null, null);
            }

            Admin admin = adminOpt.get();

            if (!admin.getIsActive()) {
                return new AdminLoginResponse(false, "Account is deactivated", null, null);
            }

            if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                return new AdminLoginResponse(false, "Invalid username or password", null, null);
            }

            // Update last login
            admin.setLastLoginAt(LocalDateTime.now());
            adminRepository.save(admin);

            // Create DTO
            AdminDto adminDto = new AdminDto();
            adminDto.setId(admin.getId());
            adminDto.setUsername(admin.getUsername());
            adminDto.setName(admin.getName());
            adminDto.setRole(admin.getRole());
            adminDto.setIsActive(admin.getIsActive());
            adminDto.setCreatedAt(admin.getCreatedAt());
            adminDto.setLastLoginAt(admin.getLastLoginAt());

            // TODO: Generate JWT token for production
            String token = "admin_token_" + admin.getId();

            return new AdminLoginResponse(true, "Login successful", token, adminDto);

        } catch (Exception e) {
            return new AdminLoginResponse(false, "An error occurred during login", null, null);
        }
    }

    public Admin createAdmin(String username, String password, String name, String role) {
        if (adminRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setName(name);
        admin.setRole(role != null ? role : "ADMIN");
        admin.setIsActive(true);

        return adminRepository.save(admin);
    }
}