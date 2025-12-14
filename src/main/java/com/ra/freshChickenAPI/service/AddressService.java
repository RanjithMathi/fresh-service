package com.ra.freshChickenAPI.service;

import com.ra.freshChickenAPI.entity.Address;
import com.ra.freshChickenAPI.entity.AddressType;
import com.ra.freshChickenAPI.entity.Customer;
import com.ra.freshChickenAPI.repository.AddressRepository;
import com.ra.freshChickenAPI.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {
    
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    
    /**
     * Get all addresses for a customer
     */
    public List<Address> getAddressesByCustomerId(Long customerId) {
        return addressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(customerId);
    }
    
    /**
     * Get a specific address by ID
     */
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }
    
    /**
     * Get default address for a customer
     */
    public Optional<Address> getDefaultAddress(Long customerId) {
        return addressRepository.findByCustomerIdAndIsDefaultTrue(customerId);
    }
    
    /**
     * Create a new address for a customer
     */
    @Transactional
    public Address createAddress(Long customerId, Address address) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        address.setCustomer(customer);
        
        // If this is the first address or explicitly set as default, make it default
        boolean hasExistingAddresses = addressRepository.existsByCustomerId(customerId);
        if (!hasExistingAddresses || address.getIsDefault()) {
            // Unset other default addresses
            addressRepository.unsetAllDefaultAddressesForCustomer(customerId);
            address.setIsDefault(true);
        }
        
        Address savedAddress = addressRepository.save(address);
        log.info("Created new address for customer {}: {}", customerId, savedAddress.getId());
        
        return savedAddress;
    }
    
    /**
     * Update an existing address
     */
    @Transactional
    public Address updateAddress(Long id, Address addressDetails) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
        
        // Verify ownership (optional but recommended)
        Long customerId = address.getCustomer().getId();
        
        address.setAddressLine1(addressDetails.getAddressLine1());
        address.setAddressLine2(addressDetails.getAddressLine2());
        address.setCity(addressDetails.getCity());
        address.setState(addressDetails.getState());
        address.setZipCode(addressDetails.getZipCode());
        address.setLandmark(addressDetails.getLandmark());
        address.setAddressType(addressDetails.getAddressType());
        address.setContactPhone(addressDetails.getContactPhone());
        
        // Handle default flag change
        if (addressDetails.getIsDefault() && !address.getIsDefault()) {
            addressRepository.unsetAllDefaultAddressesForCustomer(customerId);
            address.setIsDefault(true);
        }
        
        Address updated = addressRepository.save(address);
        log.info("Updated address {}", id);
        
        return updated;
    }
    
    /**
     * Set an address as default
     */
    @Transactional
    public Address setAsDefault(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));
        
        Long customerId = address.getCustomer().getId();
        
        // Unset all other default addresses for this customer
        addressRepository.unsetAllDefaultAddressesForCustomer(customerId);
        
        // Set this as default
        address.setIsDefault(true);
        Address updated = addressRepository.save(address);
        
        log.info("Set address {} as default for customer {}", addressId, customerId);
        
        return updated;
    }
    
    /**
     * Delete an address
     */
    @Transactional
    public void deleteAddress(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
        
        Long customerId = address.getCustomer().getId();
        boolean wasDefault = address.getIsDefault();
        
        addressRepository.delete(address);
        log.info("Deleted address {}", id);
        
        // If deleted address was default, set another address as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByCustomerId(customerId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("Set address {} as new default for customer {}", newDefault.getId(), customerId);
            }
        }
    }
    
    /**
     * Get addresses by type for a customer
     */
    public List<Address> getAddressesByType(Long customerId, AddressType type) {
        return addressRepository.findByCustomerIdAndAddressType(customerId, type);
    }
    
    /**
     * Check if customer has any addresses
     */
    public boolean hasAddresses(Long customerId) {
        return addressRepository.existsByCustomerId(customerId);
    }
    

}