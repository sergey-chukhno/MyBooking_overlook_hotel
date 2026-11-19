package com.MyBooking.announcement.domain;

import com.MyBooking.auth.domain.Role;
import com.MyBooking.auth.domain.User;
import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.common.domain.PublicIdentifiable;
import com.MyBooking.employee.domain.Department;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "announcement")
@EntityListeners(PublicIdEntityListener.class)
public class Announcement implements PublicIdentifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @NotBlank(message = "Announcement title is required")
    @Size(max = 200, message = "Announcement title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "Announcement content is required")
    @Size(max = 2000, message = "Announcement content must not exceed 2000 characters")
    @Column(name = "content", nullable = false, length = 2000)
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    @NotNull(message = "Created by user is required")
    private User createdBy;
    
    @NotNull(message = "Announcement priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private AnnouncementPriority priority;
    
    @NotNull(message = "Announcement status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AnnouncementStatus status;

    @NotNull(message = "Target department is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "target_department", nullable = false, length = 32)
    private Department targetDepartment = Department.ALL;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", length = 16)
    private Role targetRole;
    
    
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
    public Announcement() {}
    
    public Announcement(String title, String content, User createdBy, AnnouncementPriority priority, AnnouncementStatus status) {
        this(title, content, createdBy, priority, status, Department.ALL, null);
    }

    public Announcement(String title, String content, User createdBy, AnnouncementPriority priority,
                        AnnouncementStatus status, Department targetDepartment, Role targetRole) {
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.priority = priority;
        this.status = status;
        this.targetDepartment = targetDepartment != null ? targetDepartment : Department.ALL;
        this.targetRole = targetRole;
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
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    
    public AnnouncementPriority getPriority() { return priority; }
    public void setPriority(AnnouncementPriority priority) { this.priority = priority; }
    
    public AnnouncementStatus getStatus() { return status; }
    public void setStatus(AnnouncementStatus status) { this.status = status; }

    public Department getTargetDepartment() { return targetDepartment; }
    public void setTargetDepartment(Department targetDepartment) {
        this.targetDepartment = targetDepartment != null ? targetDepartment : Department.ALL;
    }

    public Role getTargetRole() { return targetRole; }
    public void setTargetRole(Role targetRole) { this.targetRole = targetRole; }
    
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}