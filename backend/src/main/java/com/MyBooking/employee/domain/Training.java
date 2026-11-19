package com.MyBooking.employee.domain;

import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "training")
@EntityListeners(PublicIdEntityListener.class)
public class Training implements PublicIdentifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @NotBlank(message = "Training title is required")
    @Size(max = 200, message = "Training title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @NotNull(message = "Training start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull(message = "Training end date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    // Constructors
    public Training() {}
    
    public Training(String title, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public UUID getPublicId() { return publicId; }

    @Override
    public void setPublicId(UUID publicId) { this.publicId = publicId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}