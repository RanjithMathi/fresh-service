package com.ra.freshChickenAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    // NEW: Reference to the address used for this order
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    private Address address;
    
    // Snapshot of address details at order time (for historical records)
    @Column(length = 500)
    private String deliveryAddressLine1;
    
    @Column(length = 500)
    private String deliveryAddressLine2;
    
    @Column
    private String deliveryCity;
    
    @Column
    private String deliveryState;
    
    @Column
    private String deliveryZipCode;
    
    @Column
    private String deliveryLandmark;
    
    @Column
    private String deliveryContactPhone;
    
    // DEPRECATED: Old single field address - keep for backward compatibility
    @Deprecated
    @Column(length = 1000)
    private String deliveryAddress;
    
    private LocalDateTime orderDate;
    
    private LocalDateTime deliveryDate;
    
    private String specialInstructions;
    
    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        
        // Copy address details from Address entity for historical record
        if (address != null) {
            this.deliveryAddressLine1 = address.getAddressLine1();
            this.deliveryAddressLine2 = address.getAddressLine2();
            this.deliveryCity = address.getCity();
            this.deliveryState = address.getState();
            this.deliveryZipCode = address.getZipCode();
            this.deliveryLandmark = address.getLandmark();
            this.deliveryContactPhone = address.getContactPhone();
            this.deliveryAddress = address.getFullAddress(); // Backward compatibility
        }
    }
    
    // Helper method to get full delivery address
    public String getFullDeliveryAddress() {
        if (deliveryAddressLine1 != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(deliveryAddressLine1);
            
            if (deliveryAddressLine2 != null && !deliveryAddressLine2.isEmpty()) {
                sb.append(", ").append(deliveryAddressLine2);
            }
            
            if (deliveryLandmark != null && !deliveryLandmark.isEmpty()) {
                sb.append(", ").append(deliveryLandmark);
            }
            
            sb.append(", ").append(deliveryCity);
            sb.append(", ").append(deliveryState);
            sb.append(" - ").append(deliveryZipCode);
            
            return sb.toString();
        }
        return deliveryAddress; // Fallback to old field
    }
}