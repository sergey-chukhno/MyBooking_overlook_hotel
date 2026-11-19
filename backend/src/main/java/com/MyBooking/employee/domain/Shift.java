package com.MyBooking.employee.domain;

import com.MyBooking.auth.domain.User;
import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shift")
@EntityListeners(PublicIdEntityListener.class)
public class Shift implements PublicIdentifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_user_id", nullable = false)
    @NotNull(message = "Employee is required")
    private User employee;
    
    @NotNull(message = "Shift start time is required")
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    
    @NotNull(message = "Shift end time is required")
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;
    
    // Constructors
    public Shift() {}
    
    public Shift(User employee, LocalDateTime startAt, LocalDateTime endAt) {
        this.employee = employee;
        this.startAt = startAt;
        this.endAt = endAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public UUID getPublicId() { return publicId; }

    @Override
    public void setPublicId(UUID publicId) { this.publicId = publicId; }
    
    public User getEmployee() { return employee; }
    public void setEmployee(User employee) { this.employee = employee; }
    
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
}