package com.ra.freshChickenAPI.controller;

import com.ra.freshChickenAPI.entity.Address;
import com.ra.freshChickenAPI.entity.AddressType;
import com.ra.freshChickenAPI.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AddressController {
    
    private final AddressService addressService;
    
    /**
     * Get all addresses for a customer
     * GET /api/addresses/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Address>> getCustomerAddresses(@PathVariable Long customerId) {
        List<Address> addresses = addressService.getAddressesByCustomerId(customerId);
        return ResponseEntity.ok(addresses);
    }
    
    /**
     * Get a specific address by ID
     * GET /api/addresses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable Long id) {
        return addressService.getAddressById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get default address for a customer
     * GET /api/addresses/customer/{customerId}/default
     */
    @GetMapping("/customer/{customerId}/default")
    public ResponseEntity<Address> getDefaultAddress(@PathVariable Long customerId) {
        return addressService.getDefaultAddress(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new address for a customer
     * POST /api/addresses/customer/{customerId}
     */
    @PostMapping("/customer/{customerId}")
    public ResponseEntity<Address> createAddress(
            @PathVariable Long customerId,
            @Valid @RequestBody Address address) {
        try {
            Address created = addressService.createAddress(customerId, address);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update an existing address
     * PUT /api/addresses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody Address address) {
        try {
            Address updated = addressService.updateAddress(id, address);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Set an address as default
     * PATCH /api/addresses/{id}/set-default
     */
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<Address> setAsDefault(@PathVariable Long id) {
        try {
            Address updated = addressService.setAsDefault(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete an address
     * DELETE /api/addresses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        try {
            addressService.deleteAddress(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get addresses by type for a customer
     * GET /api/addresses/customer/{customerId}/type/{type}
     */
    @GetMapping("/customer/{customerId}/type/{type}")
    public ResponseEntity<List<Address>> getAddressesByType(
            @PathVariable Long customerId,
            @PathVariable AddressType type) {
        List<Address> addresses = addressService.getAddressesByType(customerId, type);
        return ResponseEntity.ok(addresses);
    }
    
    /**
     * Check if customer has addresses
     * GET /api/addresses/customer/{customerId}/exists
     */
    @GetMapping("/customer/{customerId}/exists")
    public ResponseEntity<Boolean> hasAddresses(@PathVariable Long customerId) {
        boolean hasAddresses = addressService.hasAddresses(customerId);
        return ResponseEntity.ok(hasAddresses);
    }
}