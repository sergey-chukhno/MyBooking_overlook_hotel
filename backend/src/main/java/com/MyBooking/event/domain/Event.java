package com.MyBooking.event.domain;

import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import com.MyBooking.installation.domain.Installation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event")
@EntityListeners(PublicIdEntityListener.class)
public class Event implements PublicIdentifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @NotBlank(message = "Event name is required")
    @Size(max = 100, message = "Event name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @NotNull(message = "Event type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private EventType eventType;
    
    @NotNull(message = "Start time is required")
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    
    @NotNull(message = "End time is required")
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 100, message = "Capacity must not exceed 100")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installation_id", nullable = false)
    @NotNull(message = "Installation is required")
    @JsonIgnore
    private Installation installation;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Event() {}
    
    public Event(String name, EventType eventType, LocalDateTime startAt, LocalDateTime endAt,
                Integer capacity, BigDecimal price, String currency, Installation installation) {
        this.name = name;
        this.eventType = eventType;
        this.startAt = startAt;
        this.endAt = endAt;
        this.capacity = capacity;
        this.price = price;
        this.currency = currency;
        this.installation = installation;
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
    
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Installation getInstallation() { return installation; }
    public void setInstallation(Installation installation) { this.installation = installation; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Simple utility methods
    public String getFormattedPrice() {
        return currency + " " + price;
    }
}