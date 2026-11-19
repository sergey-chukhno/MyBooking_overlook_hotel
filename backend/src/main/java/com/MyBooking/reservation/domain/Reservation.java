package com.MyBooking.reservation.domain;

import com.MyBooking.auth.domain.User;
import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import com.MyBooking.room.domain.Room;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@EntityListeners(PublicIdEntityListener.class)
public class Reservation implements PublicIdentifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @NotNull(message = "Check-in date is required")
    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;
    
    @NotNull(message = "Check-out date is required")
    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;
    
    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "Number of guests must be at least 1")
    @Max(value = 10, message = "Number of guests must not exceed 10")
    @Column(name = "number_of_guests", nullable = false)
    private Integer numberOfGuests;
    
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total price must be greater than or equal to 0")
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Client is required")
    private User client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @NotNull(message = "Room is required")
    private Room room;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "points_used")
    private Integer pointsUsed = 0;
    
    @Column(name = "points_discount", precision = 12, scale = 2)
    private BigDecimal pointsDiscount = BigDecimal.ZERO;
    
    // Constructors
    public Reservation() {}
    
    public Reservation(LocalDate checkIn, LocalDate checkOut, Integer numberOfGuests, 
                      BigDecimal totalPrice, String currency, ReservationStatus status, 
                      User client, Room room) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.status = status;
        this.client = client;
        this.room = room;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public UUID getPublicId() { return publicId; }

    @Override
    public void setPublicId(UUID publicId) { this.publicId = publicId; }
    
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    
    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    
    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
    
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Integer getPointsUsed() { return pointsUsed; }
    public void setPointsUsed(Integer pointsUsed) { this.pointsUsed = pointsUsed; }
    
    public BigDecimal getPointsDiscount() { return pointsDiscount; }
    public void setPointsDiscount(BigDecimal pointsDiscount) { this.pointsDiscount = pointsDiscount; }
    
    // Utility methods
    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }
    
    public String getFormattedPrice() {
        return currency + " " + totalPrice;
    }
    
}