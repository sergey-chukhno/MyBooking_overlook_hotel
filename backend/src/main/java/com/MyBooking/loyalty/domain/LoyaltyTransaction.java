package com.MyBooking.loyalty.domain;

import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import com.MyBooking.reservation.domain.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loyalty_transaction")
@EntityListeners(PublicIdEntityListener.class)
public class LoyaltyTransaction implements PublicIdentifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "Loyalty account is required")
    private LoyaltyAccount account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    @NotNull(message = "Transaction type is required")
    private LoyaltyTxType type;

    @Min(value = 1, message = "Points must be positive")
    @Column(name = "points", nullable = false)
    @NotNull(message = "Points amount is required")
    private Integer points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public LoyaltyTransaction() {}

    public LoyaltyTransaction(LoyaltyAccount account, LoyaltyTxType type, Integer points) {
        this.account = account;
        this.type = type;
        this.points = points;
    }

    public LoyaltyTransaction(LoyaltyAccount account, LoyaltyTxType type, Integer points, Reservation reservation) {
        this.account = account;
        this.type = type;
        this.points = points;
        this.reservation = reservation;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public UUID getPublicId() { return publicId; }

    @Override
    public void setPublicId(UUID publicId) { this.publicId = publicId; }

    public LoyaltyAccount getAccount() { return account; }
    public void setAccount(LoyaltyAccount account) { this.account = account; }

    public LoyaltyTxType getType() { return type; }
    public void setType(LoyaltyTxType type) { this.type = type; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}