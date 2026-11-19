package com.MyBooking.announcement.repository;

import com.MyBooking.announcement.domain.AnnouncementReply;
import com.MyBooking.announcement.domain.Announcement;
import com.MyBooking.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnouncementReplyRepository extends JpaRepository<AnnouncementReply, Long> {

    // ==================== BASIC QUERIES ====================

    @EntityGraph(attributePaths = {"user"})
    Optional<AnnouncementReply> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<AnnouncementReply> findAll(Pageable pageable);

    // Find by announcement (entity-based)
    List<AnnouncementReply> findByAnnouncement(Announcement announcement);
    @EntityGraph(attributePaths = {"user"})
    Page<AnnouncementReply> findByAnnouncement(Announcement announcement, Pageable pageable);

    // Find by announcement (ID-based)
    List<AnnouncementReply> findByAnnouncementId(Long announcementId);
    @EntityGraph(attributePaths = {"user"})
    Page<AnnouncementReply> findByAnnouncementId(Long announcementId, Pageable pageable);

    // Find by user (entity-based) - CORRECTED: using 'user' not 'replier'
    List<AnnouncementReply> findByUser(User user);
    @EntityGraph(attributePaths = {"user"})
    Page<AnnouncementReply> findByUser(User user, Pageable pageable);

    // Find by user (ID-based) - CORRECTED: using 'userId' not 'replierId'
    List<AnnouncementReply> findByUserId(Long userId);
    @EntityGraph(attributePaths = {"user"})
    Page<AnnouncementReply> findByUserId(Long userId, Pageable pageable);

    // ==================== DATE-BASED QUERIES ====================

    // Find by creation date range
    List<AnnouncementReply> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<AnnouncementReply> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by creation date after
    List<AnnouncementReply> findByCreatedAtAfter(LocalDateTime date);
    Page<AnnouncementReply> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    // Find by creation date before
    List<AnnouncementReply> findByCreatedAtBefore(LocalDateTime date);
    Page<AnnouncementReply> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);

    // ==================== COMBINED QUERIES ====================

    // Find by announcement and user - CORRECTED: using 'user' not 'replier'
    List<AnnouncementReply> findByAnnouncementAndUser(Announcement announcement, User user);
    Page<AnnouncementReply> findByAnnouncementAndUser(Announcement announcement, User user, Pageable pageable);
    List<AnnouncementReply> findByAnnouncementIdAndUserId(Long announcementId, Long userId);
    Page<AnnouncementReply> findByAnnouncementIdAndUserId(Long announcementId, Long userId, Pageable pageable);

    // ==================== CUSTOM BUSINESS QUERIES ====================

    // Find recent replies (business logic in repository - following current pattern)
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.createdAt >= :since ORDER BY ar.createdAt DESC")
    List<AnnouncementReply> findRecentReplies(@Param("since") LocalDateTime since);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.createdAt >= :since ORDER BY ar.createdAt DESC")
    Page<AnnouncementReply> findRecentReplies(@Param("since") LocalDateTime since, Pageable pageable);

    // Find replies by announcement ordered by creation date (business logic in repository)
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.announcement = :announcement ORDER BY ar.createdAt ASC")
    List<AnnouncementReply> findByAnnouncementOrderByCreatedAtAsc(@Param("announcement") Announcement announcement);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.announcement = :announcement ORDER BY ar.createdAt ASC")
    Page<AnnouncementReply> findByAnnouncementOrderByCreatedAtAsc(@Param("announcement") Announcement announcement, Pageable pageable);

    // Find replies by announcement ID ordered by creation date (business logic in repository)
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.announcement.id = :announcementId ORDER BY ar.createdAt ASC")
    List<AnnouncementReply> findByAnnouncementIdOrderByCreatedAtAsc(@Param("announcementId") Long announcementId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.announcement.id = :announcementId ORDER BY ar.createdAt ASC")
    Page<AnnouncementReply> findByAnnouncementIdOrderByCreatedAtAsc(@Param("announcementId") Long announcementId, Pageable pageable);

    // Find replies by user ordered by creation date (business logic in repository) - CORRECTED
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.user = :user ORDER BY ar.createdAt DESC")
    List<AnnouncementReply> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.user = :user ORDER BY ar.createdAt DESC")
    Page<AnnouncementReply> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    // Find replies by user ID ordered by creation date (business logic in repository) - CORRECTED
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.user.id = :userId ORDER BY ar.createdAt DESC")
    List<AnnouncementReply> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.user.id = :userId ORDER BY ar.createdAt DESC")
    Page<AnnouncementReply> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // ==================== COUNT QUERIES ====================

    // Count by announcement
    long countByAnnouncement(Announcement announcement);
    long countByAnnouncementId(Long announcementId);

    // Count by user 
    long countByUser(User user);
    long countByUserId(Long userId);

    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBefore(LocalDateTime date);

    // Count by announcement and user 
    long countByAnnouncementAndUser(Announcement announcement, User user);
    long countByAnnouncementIdAndUserId(Long announcementId, Long userId);

    // ==================== EXISTENCE QUERIES ====================

    // Check if reply exists by announcement
    boolean existsByAnnouncement(Announcement announcement);
    boolean existsByAnnouncementId(Long announcementId);

    // Check if reply exists by user 
    boolean existsByUser(User user);
    boolean existsByUserId(Long userId);

    // Check if reply exists by announcement and user 
    boolean existsByAnnouncementAndUser(Announcement announcement, User user);
    boolean existsByAnnouncementIdAndUserId(Long announcementId, Long userId);

    // ==================== AGGREGATION QUERIES ====================

    // Get latest reply by announcement (business logic in repository)
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.announcement = :announcement ORDER BY ar.createdAt DESC")
    List<AnnouncementReply> findLatestReplyByAnnouncement(@Param("announcement") Announcement announcement, Pageable pageable);

    // Get latest reply by announcement ID (business logic in repository)
    @Query("SELECT ar FROM AnnouncementReply ar WHERE ar.announcement.id = :announcementId ORDER BY ar.createdAt DESC")
    List<AnnouncementReply> findLatestReplyByAnnouncementId(@Param("announcementId") Long announcementId, Pageable pageable);

    // Get reply count by announcement
    @Query("SELECT a, COUNT(ar) FROM Announcement a LEFT JOIN AnnouncementReply ar ON a.id = ar.announcement.id GROUP BY a.id")
    List<Object[]> getReplyCountByAnnouncement();
}