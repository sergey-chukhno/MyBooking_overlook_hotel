package com.MyBooking.room.domain;

import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipment")
@EntityListeners(PublicIdEntityListener.class)
public class Equipment implements PublicIdentifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @NotBlank(message = "Equipment name is required")
    @Size(max = 100, message = "Equipment name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @NotNull(message = "Equipment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_type", nullable = false, length = 32)
    private EquipmentType equipmentType;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity must not exceed 100")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Available quantity must be at least 0")
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    @Column(name = "brand", length = 50)
    private String brand;
    
    @Size(max = 50, message = "Model must not exceed 50 characters")
    @Column(name = "model", length = 50)
    private String model;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Equipment() {}
    
    public Equipment(String name, EquipmentType equipmentType, Integer quantity, String brand, String model) {
        this.name = name;
        this.equipmentType = equipmentType;
        this.quantity = quantity;
        this.availableQuantity = quantity;
        this.brand = brand;
        this.model = model;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public UUID getPublicId() { return publicId; }

    @Override
    public void setPublicId(UUID publicId) { this.publicId = publicId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Business methods
    public boolean isAvailable() {
        return isActive && availableQuantity > 0;
    }
    
    public void reserveEquipment(int quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
        } else {
            throw new IllegalArgumentException("Not enough equipment available");
        }
    }
    
    public void releaseEquipment(int quantity) {
        if (availableQuantity + quantity <= this.quantity) {
            availableQuantity += quantity;
        } else {
            throw new IllegalArgumentException("Cannot release more equipment than total quantity");
        }
    }
}
