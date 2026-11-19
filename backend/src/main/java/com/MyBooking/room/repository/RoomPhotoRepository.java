package com.MyBooking.room.repository;

import com.MyBooking.room.domain.Room;
import com.MyBooking.room.domain.RoomPhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomPhotoRepository extends JpaRepository<RoomPhoto, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<RoomPhoto> findByPublicId(UUID publicId);

    Optional<RoomPhoto> findByPublicIdAndRoomId(UUID publicId, Long roomId);

    boolean existsByPublicId(UUID publicId);

    // Find by room
    List<RoomPhoto> findByRoom(Room room);
    Page<RoomPhoto> findByRoom(Room room, Pageable pageable);
    
    // Find by room ID
    List<RoomPhoto> findByRoomId(Long roomId);
    Page<RoomPhoto> findByRoomId(Long roomId, Pageable pageable);

    // ==================== ACTIVE PHOTOS QUERIES ====================

    // Find active photos
    List<RoomPhoto> findByIsActiveTrue();
    Page<RoomPhoto> findByIsActiveTrue(Pageable pageable);
    
    // Find active photos by room
    List<RoomPhoto> findByRoomAndIsActiveTrue(Room room);
    Page<RoomPhoto> findByRoomAndIsActiveTrue(Room room, Pageable pageable);
    
    // Find active photos by room ID
    List<RoomPhoto> findByRoomIdAndIsActiveTrue(Long roomId);
    Page<RoomPhoto> findByRoomIdAndIsActiveTrue(Long roomId, Pageable pageable);

    // ==================== PRIMARY PHOTO QUERIES ====================

    // Find primary photos
    List<RoomPhoto> findByIsPrimaryTrue();
    Page<RoomPhoto> findByIsPrimaryTrue(Pageable pageable);
    
    // Find primary photo by room
    Optional<RoomPhoto> findByRoomAndIsPrimaryTrue(Room room);
    Optional<RoomPhoto> findByRoomIdAndIsPrimaryTrue(Long roomId);
    
    // Find primary active photos by room
    Optional<RoomPhoto> findByRoomAndIsPrimaryTrueAndIsActiveTrue(Room room);
    Optional<RoomPhoto> findByRoomIdAndIsPrimaryTrueAndIsActiveTrue(Long roomId);

    // ==================== ORDERING QUERIES ====================

    // Find photos by room ordered by display order
    List<RoomPhoto> findByRoomOrderByDisplayOrderAsc(Room room);
    List<RoomPhoto> findByRoomIdOrderByDisplayOrderAsc(Long roomId);
    
    // Find active photos by room ordered by display order
    List<RoomPhoto> findByRoomAndIsActiveTrueOrderByDisplayOrderAsc(Room room);
    List<RoomPhoto> findByRoomIdAndIsActiveTrueOrderByDisplayOrderAsc(Long roomId);
    
    // Find photos by room ordered by creation date
    List<RoomPhoto> findByRoomOrderByCreatedAtAsc(Room room);
    List<RoomPhoto> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // ==================== SEARCH QUERIES ====================

    // Find by caption containing (case-insensitive)
    List<RoomPhoto> findByCaptionContainingIgnoreCase(String caption);
    Page<RoomPhoto> findByCaptionContainingIgnoreCase(String caption, Pageable pageable);
    
    // Find by photo type
    List<RoomPhoto> findByPhotoType(String photoType);
    Page<RoomPhoto> findByPhotoType(String photoType, Pageable pageable);
    
    // Find by file name
    List<RoomPhoto> findByFileName(String fileName);
    Page<RoomPhoto> findByFileName(String fileName, Pageable pageable);
    
    // Find by file name containing (case-insensitive)
    List<RoomPhoto> findByFileNameContainingIgnoreCase(String fileName);
    Page<RoomPhoto> findByFileNameContainingIgnoreCase(String fileName, Pageable pageable);

    // ==================== COMBINED QUERIES ====================

    // Find by room and photo type
    List<RoomPhoto> findByRoomAndPhotoType(Room room, String photoType);
    Page<RoomPhoto> findByRoomAndPhotoType(Room room, String photoType, Pageable pageable);
    
    // Find by room ID and photo type
    List<RoomPhoto> findByRoomIdAndPhotoType(Long roomId, String photoType);
    Page<RoomPhoto> findByRoomIdAndPhotoType(Long roomId, String photoType, Pageable pageable);
    
    // Find by room and display order range
    List<RoomPhoto> findByRoomAndDisplayOrderBetween(Room room, Integer minOrder, Integer maxOrder);
    Page<RoomPhoto> findByRoomAndDisplayOrderBetween(Room room, Integer minOrder, Integer maxOrder, Pageable pageable);
    
    // Find by room ID and display order range
    List<RoomPhoto> findByRoomIdAndDisplayOrderBetween(Long roomId, Integer minOrder, Integer maxOrder);
    Page<RoomPhoto> findByRoomIdAndDisplayOrderBetween(Long roomId, Integer minOrder, Integer maxOrder, Pageable pageable);

    // ==================== DATE-BASED QUERIES ====================

    // Find by creation date range
    List<RoomPhoto> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<RoomPhoto> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by creation date after
    List<RoomPhoto> findByCreatedAtAfter(LocalDateTime date);
    Page<RoomPhoto> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);
    
    // Find by creation date before
    List<RoomPhoto> findByCreatedAtBefore(LocalDateTime date);
    Page<RoomPhoto> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);
    
    // Find by room and creation date range
    List<RoomPhoto> findByRoomAndCreatedAtBetween(Room room, LocalDateTime startDate, LocalDateTime endDate);
    Page<RoomPhoto> findByRoomAndCreatedAtBetween(Room room, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // ==================== CUSTOM BUSINESS QUERIES ====================

    // Find photos by room with specific display order
    List<RoomPhoto> findByRoomAndDisplayOrder(Room room, Integer displayOrder);
    List<RoomPhoto> findByRoomIdAndDisplayOrder(Long roomId, Integer displayOrder);
    
    // Find next display order for a room
    @Query("SELECT COALESCE(MAX(rp.displayOrder), 0) + 1 FROM RoomPhoto rp WHERE rp.room = :room")
    Integer findNextDisplayOrderForRoom(@Param("room") Room room);
    
    @Query("SELECT COALESCE(MAX(rp.displayOrder), 0) + 1 FROM RoomPhoto rp WHERE rp.room.id = :roomId")
    Integer findNextDisplayOrderForRoomId(@Param("roomId") Long roomId);
    
    // Find photos by file size range
    List<RoomPhoto> findByFileSizeBetween(Long minSize, Long maxSize);
    Page<RoomPhoto> findByFileSizeBetween(Long minSize, Long maxSize, Pageable pageable);
    
    // Find large photos
    @Query("SELECT rp FROM RoomPhoto rp WHERE rp.fileSize > :threshold")
    List<RoomPhoto> findLargePhotos(@Param("threshold") Long sizeThreshold);
    @Query("SELECT rp FROM RoomPhoto rp WHERE rp.fileSize > :threshold")
    Page<RoomPhoto> findLargePhotos(@Param("threshold") Long sizeThreshold, Pageable pageable);

    // ==================== COUNT QUERIES ====================

    // Count by room
    long countByRoom(Room room);
    long countByRoomId(Long roomId);
    
    // Count active photos by room
    long countByRoomAndIsActiveTrue(Room room);
    long countByRoomIdAndIsActiveTrue(Long roomId);
    
    // Count primary photos by room
    long countByRoomAndIsPrimaryTrue(Room room);
    long countByRoomIdAndIsPrimaryTrue(Long roomId);
    
    // Count by photo type
    long countByPhotoType(String photoType);
    
    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count by file size range
    long countByFileSizeBetween(Long minSize, Long maxSize);

    // ==================== EXISTENCE QUERIES ====================

    // Check existence by room
    boolean existsByRoom(Room room);
    boolean existsByRoomId(Long roomId);
    
    // Check existence by room and active status
    boolean existsByRoomAndIsActiveTrue(Room room);
    boolean existsByRoomIdAndIsActiveTrue(Long roomId);
    
    // Check existence by room and primary status
    boolean existsByRoomAndIsPrimaryTrue(Room room);
    boolean existsByRoomIdAndIsPrimaryTrue(Long roomId);
    
    // Check existence by photo URL
    boolean existsByPhotoUrl(String photoUrl);
    
    // Check existence by file name
    boolean existsByFileName(String fileName);

    // ==================== AGGREGATION QUERIES ====================

    // Get total number of photos
    long count();

    // Get photo count by room
    @Query("SELECT rp.room.id, COUNT(rp) FROM RoomPhoto rp GROUP BY rp.room.id")
    List<Object[]> getPhotoCountByRoom();
    
    // Get active photo count by room
    @Query("SELECT rp.room.id, COUNT(rp) FROM RoomPhoto rp WHERE rp.isActive = true GROUP BY rp.room.id")
    List<Object[]> getActivePhotoCountByRoom();
    
    // Get average file size by photo type
    @Query("SELECT rp.photoType, AVG(rp.fileSize) FROM RoomPhoto rp WHERE rp.fileSize IS NOT NULL GROUP BY rp.photoType")
    List<Object[]> getAverageFileSizeByPhotoType();
    
    // Get total file size by room
    @Query("SELECT rp.room.id, SUM(rp.fileSize) FROM RoomPhoto rp WHERE rp.fileSize IS NOT NULL GROUP BY rp.room.id")
    List<Object[]> getTotalFileSizeByRoom();
}
