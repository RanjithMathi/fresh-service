package com.ra.freshChickenAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String name;
    
    @Column(unique = true)
    private String email;
    
    @Column(nullable = false, unique = true, length = 10)
    private String phone;
    
    // DEPRECATED: Keep for backward compatibility, but use Address entity instead
    @Deprecated
    @Column
    private String address;
    
    @Deprecated
    private String city;
    
    @Deprecated
    private String zipCode;
    
    // NEW: One-to-Many relationship with Address
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Address> addresses = new ArrayList<>();

    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private LocalDateTime lastLoginAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Helper method to get default address
    public Address getDefaultAddress() {
        // Defensive null check
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        
        return addresses.stream()
                .filter(Address::getIsDefault)
                .findFirst()
                .orElse(addresses.get(0));
    }
    
    // Helper method to add address
    public void addAddress(Address address) {
        addresses.add(address);
        address.setCustomer(this);
        
        // If this is the first address, make it default
        if (addresses.size() == 1) {
            address.setIsDefault(true);
        }
    }
    
    // Helper method to remove address
    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setCustomer(null);
    }
}