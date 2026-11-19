package com.MyBooking.installation.domain;

import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "installation")
@EntityListeners(PublicIdEntityListener.class)
public class Installation implements PublicIdentifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @NotBlank(message = "Installation name is required")
    @Size(max = 100, message = "Installation name must not exceed 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @NotNull(message = "Installation type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "installation_type", nullable = false, length = 32)
    private InstallationType installationType;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 200, message = "Capacity must not exceed 200")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.1", inclusive = false, message = "Hourly rate must be greater than 0")
    @Column(name = "hourly_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Size(max = 255, message = "Equipment must not exceed 255 characters")
    @Column(name = "equipment", length = 255)
    private String equipment;
    
    @OneToMany(mappedBy = "installation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<com.MyBooking.event.domain.Event> events = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors
    public Installation() {}
    
    public Installation(String name, InstallationType installationType, Integer capacity, 
                       BigDecimal hourlyRate, String currency) {
        this.name = name;
        this.installationType = installationType;
        this.capacity = capacity;
        this.hourlyRate = hourlyRate;
        this.currency = currency;
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
    
    public InstallationType getInstallationType() { return installationType; }
    public void setInstallationType(InstallationType installationType) { this.installationType = installationType; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<com.MyBooking.event.domain.Event> getEvents() { return events; }
    public void setEvents(List<com.MyBooking.event.domain.Event> events) { this.events = events; }
    
    // Simple utility methods
    public String getFormattedHourlyRate() {
        return currency + " " + hourlyRate + "/hour";
    }
}