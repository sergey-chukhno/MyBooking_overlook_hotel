package com.MyBooking.announcement.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.MyBooking.announcement.domain.Announcement;
import com.MyBooking.announcement.domain.AnnouncementPriority;
import com.MyBooking.announcement.domain.AnnouncementStatus;
import com.MyBooking.auth.domain.Role;
import com.MyBooking.auth.domain.User;
import com.MyBooking.employee.domain.Department;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    // ==================== BASIC QUERIES ====================

    @EntityGraph(attributePaths = {"createdBy"})
    Optional<Announcement> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    @Override
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Announcement> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Announcement a WHERE a.id = :id")
    java.util.Optional<Announcement> findByIdForUpdate(@Param("id") Long id);

    // Find by created by user (entity-based)
    List<Announcement> findByCreatedBy(User createdBy);
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Announcement> findByCreatedBy(User createdBy, Pageable pageable);
    
    // Find by created by user (ID-based)
    List<Announcement> findByCreatedById(Long createdById);
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Announcement> findByCreatedById(Long createdById, Pageable pageable);
    
    // Find by priority
    List<Announcement> findByPriority(AnnouncementPriority priority);
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Announcement> findByPriority(AnnouncementPriority priority, Pageable pageable);
    
    // Find by status
    List<Announcement> findByStatus(AnnouncementStatus status);
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Announcement> findByStatus(AnnouncementStatus status, Pageable pageable);
    
    // ==================== DATE-BASED QUERIES ====================
    
    // Find by creation date range
    List<Announcement> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Announcement> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by creation date after
    List<Announcement> findByCreatedAtAfter(LocalDateTime date);
    Page<Announcement> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);
    
    // Find by creation date before
    List<Announcement> findByCreatedAtBefore(LocalDateTime date);
    Page<Announcement> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);
    
    
    // ==================== COMBINED QUERIES ====================
    
    // Find by priority and status
    List<Announcement> findByPriorityAndStatus(AnnouncementPriority priority, AnnouncementStatus status);
    Page<Announcement> findByPriorityAndStatus(AnnouncementPriority priority, AnnouncementStatus status, Pageable pageable);
    
    // Find by created by user and status
    List<Announcement> findByCreatedByAndStatus(User createdBy, AnnouncementStatus status);
    Page<Announcement> findByCreatedByAndStatus(User createdBy, AnnouncementStatus status, Pageable pageable);
    List<Announcement> findByCreatedByIdAndStatus(Long createdById, AnnouncementStatus status);
    Page<Announcement> findByCreatedByIdAndStatus(Long createdById, AnnouncementStatus status, Pageable pageable);
    
    // Find by created by user and priority
    List<Announcement> findByCreatedByAndPriority(User createdBy, AnnouncementPriority priority);
    Page<Announcement> findByCreatedByAndPriority(User createdBy, AnnouncementPriority priority, Pageable pageable);
    List<Announcement> findByCreatedByIdAndPriority(Long createdById, AnnouncementPriority priority);
    Page<Announcement> findByCreatedByIdAndPriority(Long createdById, AnnouncementPriority priority, Pageable pageable);
    
    // ==================== CUSTOM BUSINESS QUERIES ====================
    
    // Find recent announcements (business logic in repository - following current pattern)
    @Query("SELECT a FROM Announcement a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Announcement> findRecentAnnouncements(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM Announcement a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    Page<Announcement> findRecentAnnouncements(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Find published announcements ordered by priority and creation date (business logic in repository)
    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' ORDER BY " +
           "CASE a.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 END, " +
           "a.createdAt DESC")
    List<Announcement> findPublishedAnnouncementsOrderByPriorityAndDate();
    
    @Query("SELECT a FROM Announcement a WHERE a.status = 'PUBLISHED' ORDER BY " +
           "CASE a.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 END, " +
           "a.createdAt DESC")
    Page<Announcement> findPublishedAnnouncementsOrderByPriorityAndDate(Pageable pageable);
    
    // Find announcements by title containing text (business logic in repository)
    @Query("SELECT a FROM Announcement a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Announcement> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    @Query("SELECT a FROM Announcement a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Announcement> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);
    
    // Find announcements by content containing text (business logic in repository)
    @Query("SELECT a FROM Announcement a WHERE LOWER(a.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    List<Announcement> findByContentContainingIgnoreCase(@Param("content") String content);
    
    @Query("SELECT a FROM Announcement a WHERE LOWER(a.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    Page<Announcement> findByContentContainingIgnoreCase(@Param("content") String content, Pageable pageable);
    
    // Find announcements by created by user ordered by creation date (business logic in repository)
    @Query("SELECT a FROM Announcement a WHERE a.createdBy = :createdBy ORDER BY a.createdAt DESC")
    List<Announcement> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") User createdBy);
    
    @Query("SELECT a FROM Announcement a WHERE a.createdBy = :createdBy ORDER BY a.createdAt DESC")
    Page<Announcement> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") User createdBy, Pageable pageable);
    
    // Find announcements by created by user ID ordered by creation date (business logic in repository)
    @Query("SELECT a FROM Announcement a WHERE a.createdBy.id = :createdById ORDER BY a.createdAt DESC")
    List<Announcement> findByCreatedByIdOrderByCreatedAtDesc(@Param("createdById") Long createdById);
    
    @Query("SELECT a FROM Announcement a WHERE a.createdBy.id = :createdById ORDER BY a.createdAt DESC")
    Page<Announcement> findByCreatedByIdOrderByCreatedAtDesc(@Param("createdById") Long createdById, Pageable pageable);
    
    // ==================== COUNT QUERIES ====================
    
    // Count by created by user
    long countByCreatedBy(User createdBy);
    long countByCreatedById(Long createdById);
    
    // Count by priority
    long countByPriority(AnnouncementPriority priority);
    
    // Count by status
    long countByStatus(AnnouncementStatus status);
    
    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBefore(LocalDateTime date);
    
    
    // Count by priority and status
    long countByPriorityAndStatus(AnnouncementPriority priority, AnnouncementStatus status);
    
    // Count by created by user and status
    long countByCreatedByAndStatus(User createdBy, AnnouncementStatus status);
    long countByCreatedByIdAndStatus(Long createdById, AnnouncementStatus status);
    
    // Count by created by user and priority
    long countByCreatedByAndPriority(User createdBy, AnnouncementPriority priority);
    long countByCreatedByIdAndPriority(Long createdById, AnnouncementPriority priority);
    
    // ==================== EXISTENCE QUERIES ====================
    
    // Check if announcement exists by created by user
    boolean existsByCreatedBy(User createdBy);
    boolean existsByCreatedById(Long createdById);
    
    // Check if announcement exists by priority
    boolean existsByPriority(AnnouncementPriority priority);
    
    // Check if announcement exists by status
    boolean existsByStatus(AnnouncementStatus status);
    
    // Check if announcement exists by priority and status
    boolean existsByPriorityAndStatus(AnnouncementPriority priority, AnnouncementStatus status);
    
    // Check if announcement exists by created by user and status
    boolean existsByCreatedByAndStatus(User createdBy, AnnouncementStatus status);
    boolean existsByCreatedByIdAndStatus(Long createdById, AnnouncementStatus status);
    
    // Check if announcement exists by created by user and priority
    boolean existsByCreatedByAndPriority(User createdBy, AnnouncementPriority priority);
    boolean existsByCreatedByIdAndPriority(Long createdById, AnnouncementPriority priority);
    
    // ==================== AGGREGATION QUERIES ====================
    
    // Get latest announcement by created by user (business logic in repository)
    @Query("SELECT a FROM Announcement a WHERE a.createdBy = :createdBy ORDER BY a.createdAt DESC")
    List<Announcement> findLatestAnnouncementByCreatedBy(@Param("createdBy") User createdBy, Pageable pageable);
    
    // Get latest announcement by created by user ID (business logic in repository)
    @Query("SELECT a FROM Announcement a WHERE a.createdBy.id = :createdById ORDER BY a.createdAt DESC")
    List<Announcement> findLatestAnnouncementByCreatedById(@Param("createdById") Long createdById, Pageable pageable);
    
    // Get announcement count by priority
    @Query("SELECT a.priority, COUNT(a) FROM Announcement a GROUP BY a.priority")
    List<Object[]> getAnnouncementCountByPriority();
    
    // Get announcement count by status
    @Query("SELECT a.status, COUNT(a) FROM Announcement a GROUP BY a.status")
    List<Object[]> getAnnouncementCountByStatus();
    
    // Get announcement count by created by user
    @Query("SELECT a.createdBy.id, COUNT(a) FROM Announcement a GROUP BY a.createdBy.id")
    List<Object[]> getAnnouncementCountByCreatedBy();

    // ==================== AUDIENCE SCOPING QUERIES ====================

    @EntityGraph(attributePaths = {"createdBy"})
    @Query("SELECT a FROM Announcement a WHERE a.status = :status " +
           "AND (a.targetDepartment = com.MyBooking.employee.domain.Department.ALL OR a.targetDepartment = :department) " +
           "AND (a.targetRole IS NULL OR a.targetRole = :role) " +
           "ORDER BY CASE a.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END, a.createdAt DESC")
    Page<Announcement> findVisibleAnnouncementsForAudience(
            @Param("department") Department department,
            @Param("role") Role role,
            @Param("status") AnnouncementStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy"})
    @Query("SELECT a FROM Announcement a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Announcement> searchByTitleOrContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT a FROM Announcement a ORDER BY (SELECT COUNT(r) FROM AnnouncementReply r WHERE r.announcement = a) DESC, a.createdAt DESC")
    List<Announcement> findMostRepliedAnnouncements(Pageable pageable);
}