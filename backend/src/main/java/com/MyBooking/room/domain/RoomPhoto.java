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
@Table(name = "room_photo")
@EntityListeners(PublicIdEntityListener.class)
public class RoomPhoto implements PublicIdentifiable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", columnDefinition = "uuid", nullable = false, unique = true, updatable = false)
    private UUID publicId;
    
    @NotBlank(message = "Photo URL is required")
    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;
    
    @Size(max = 200, message = "Caption must not exceed 200 characters")
    @Column(name = "caption", length = 200)
    private String caption;
    
    @NotNull(message = "Room is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    
    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
    
    @NotNull(message = "Is primary is required")
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
    
    @NotNull(message = "Is active is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Size(max = 50, message = "Photo type must not exceed 50 characters")
    @Column(name = "photo_type", length = 50)
    private String photoType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Size(max = 100, message = "File name must not exceed 100 characters")
    @Column(name = "file_name", length = 100)
    private String fileName;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public RoomPhoto() {}
    
    public RoomPhoto(String photoUrl, Room room, Integer displayOrder, String caption) {
        this.photoUrl = photoUrl;
        this.room = room;
        this.displayOrder = displayOrder;
        this.caption = caption;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public UUID getPublicId() { return publicId; }

    @Override
    public void setPublicId(UUID publicId) { this.publicId = publicId; }
    
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getPhotoType() { return photoType; }
    public void setPhotoType(String photoType) { this.photoType = photoType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Business methods
    public boolean isPrimaryPhoto() {
        return Boolean.TRUE.equals(isPrimary);
    }
    
    public boolean isActivePhoto() {
        return Boolean.TRUE.equals(isActive);
    }
}
