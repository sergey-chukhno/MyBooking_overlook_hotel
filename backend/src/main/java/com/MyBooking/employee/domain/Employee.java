package com.MyBooking.employee.domain;

import com.MyBooking.auth.domain.User;
import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee")
@EntityListeners(PublicIdEntityListener.class)
public class Employee implements PublicIdentifiable {
    
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId
    @NotNull(message = "User is required")
    private User user;
    
    @NotNull(message = "Employee status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private EmployeeStatus status;
    
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @NotNull(message = "Department is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false, length = 32)
    private Department department = Department.ALL;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Employee() {}
    
    public Employee(User user, EmployeeStatus status, String jobTitle) {
        this(user, status, jobTitle, Department.ALL);
    }

    public Employee(User user, EmployeeStatus status, String jobTitle, Department department) {
        this.user = user;
        this.status = status;
        this.jobTitle = jobTitle;
        this.department = department != null ? department : Department.ALL;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public UUID getPublicId() { return publicId; }

    @Override
    public void setPublicId(UUID publicId) { this.publicId = publicId; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public EmployeeStatus getStatus() { return status; }
    public void setStatus(EmployeeStatus status) { this.status = status; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) {
        this.department = department != null ? department : Department.ALL;
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Version
    @Column(name = "version")
    private Long version;
}