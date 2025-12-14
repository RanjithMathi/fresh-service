package com.ra.freshChickenAPI.repository;

import com.ra.freshChickenAPI.entity.Address;
import com.ra.freshChickenAPI.entity.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // Find all addresses for a customer
    List<Address> findByCustomerId(Long customerId);
    
    // Find all addresses for a customer ordered by default first
    List<Address> findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(Long customerId);
    
    // Find default address for a customer
    Optional<Address> findByCustomerIdAndIsDefaultTrue(Long customerId);
    
    // Find addresses by type for a customer
    List<Address> findByCustomerIdAndAddressType(Long customerId, AddressType addressType);
    
    // Check if customer has any addresses
    boolean existsByCustomerId(Long customerId);
    
    // Count addresses for a customer
    long countByCustomerId(Long customerId);
    
    // Set all addresses as non-default for a customer (used before setting a new default)
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void unsetAllDefaultAddressesForCustomer(@Param("customerId") Long customerId);
}