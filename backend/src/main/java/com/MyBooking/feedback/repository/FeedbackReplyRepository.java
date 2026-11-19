package com.MyBooking.feedback.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.MyBooking.feedback.domain.FeedbackReply;
import com.MyBooking.feedback.domain.Feedback;
import com.MyBooking.auth.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackReplyRepository extends JpaRepository<FeedbackReply, Long> {
    
    // ==================== BASIC QUERIES ====================

    @EntityGraph(attributePaths = {"adminUser", "feedback"})
    Optional<FeedbackReply> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    @Override
    @EntityGraph(attributePaths = {"adminUser", "feedback"})
    Page<FeedbackReply> findAll(Pageable pageable);

    // Find by feedback (entity-based)
    List<FeedbackReply> findByFeedback(Feedback feedback);
    Page<FeedbackReply> findByFeedback(Feedback feedback, Pageable pageable);
    
    // Find by feedback (ID-based)
    List<FeedbackReply> findByFeedbackId(Long feedbackId);
    @EntityGraph(attributePaths = {"adminUser", "feedback"})
    Page<FeedbackReply> findByFeedbackId(Long feedbackId, Pageable pageable);

    @EntityGraph(attributePaths = {"adminUser"})
    List<FeedbackReply> findByFeedbackIdInOrderByCreatedAtAsc(List<Long> feedbackIds);
    
    // Find by admin user (entity-based)
    List<FeedbackReply> findByAdminUser(User adminUser);
    @EntityGraph(attributePaths = {"adminUser", "feedback"})
    Page<FeedbackReply> findByAdminUser(User adminUser, Pageable pageable);
    
    // Find by admin user (ID-based)
    List<FeedbackReply> findByAdminUserId(Long adminUserId);
    @EntityGraph(attributePaths = {"adminUser", "feedback"})
    Page<FeedbackReply> findByAdminUserId(Long adminUserId, Pageable pageable);
    
    // ==================== DATE-BASED QUERIES ====================
    
    // Find by creation date range
    List<FeedbackReply> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<FeedbackReply> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by creation date after
    List<FeedbackReply> findByCreatedAtAfter(LocalDateTime date);
    @EntityGraph(attributePaths = {"adminUser", "feedback"})
    Page<FeedbackReply> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);
    
    // Find by creation date before
    List<FeedbackReply> findByCreatedAtBefore(LocalDateTime date);
    Page<FeedbackReply> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);
    
    // ==================== COMBINED QUERIES ====================
    
    // Find by feedback and admin user
    List<FeedbackReply> findByFeedbackAndAdminUser(Feedback feedback, User adminUser);
    Page<FeedbackReply> findByFeedbackAndAdminUser(Feedback feedback, User adminUser, Pageable pageable);
    List<FeedbackReply> findByFeedbackIdAndAdminUserId(Long feedbackId, Long adminUserId);
    Page<FeedbackReply> findByFeedbackIdAndAdminUserId(Long feedbackId, Long adminUserId, Pageable pageable);
    
    // ==================== CUSTOM BUSINESS QUERIES ====================
    
    // Find recent replies (business logic in repository - following current pattern)
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.createdAt >= :since ORDER BY fr.createdAt DESC")
    List<FeedbackReply> findRecentReplies(@Param("since") LocalDateTime since);
    
    @EntityGraph(attributePaths = {"adminUser", "feedback"})
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.createdAt >= :since ORDER BY fr.createdAt DESC")
    Page<FeedbackReply> findRecentReplies(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Find replies by feedback ordered by creation date (business logic in repository)
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.feedback = :feedback ORDER BY fr.createdAt ASC")
    List<FeedbackReply> findByFeedbackOrderByCreatedAtAsc(@Param("feedback") Feedback feedback);
    
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.feedback = :feedback ORDER BY fr.createdAt ASC")
    Page<FeedbackReply> findByFeedbackOrderByCreatedAtAsc(@Param("feedback") Feedback feedback, Pageable pageable);
    
    // Find replies by feedback ID ordered by creation date (business logic in repository)
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.feedback.id = :feedbackId ORDER BY fr.createdAt ASC")
    List<FeedbackReply> findByFeedbackIdOrderByCreatedAtAsc(@Param("feedbackId") Long feedbackId);
    
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.feedback.id = :feedbackId ORDER BY fr.createdAt ASC")
    Page<FeedbackReply> findByFeedbackIdOrderByCreatedAtAsc(@Param("feedbackId") Long feedbackId, Pageable pageable);
    
    // Find replies by admin user ordered by creation date (business logic in repository)
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.adminUser = :adminUser ORDER BY fr.createdAt DESC")
    List<FeedbackReply> findByAdminUserOrderByCreatedAtDesc(@Param("adminUser") User adminUser);
    
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.adminUser = :adminUser ORDER BY fr.createdAt DESC")
    Page<FeedbackReply> findByAdminUserOrderByCreatedAtDesc(@Param("adminUser") User adminUser, Pageable pageable);
    
    // Find replies by admin user ID ordered by creation date (business logic in repository)
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.adminUser.id = :adminUserId ORDER BY fr.createdAt DESC")
    List<FeedbackReply> findByAdminUserIdOrderByCreatedAtDesc(@Param("adminUserId") Long adminUserId);
    
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.adminUser.id = :adminUserId ORDER BY fr.createdAt DESC")
    Page<FeedbackReply> findByAdminUserIdOrderByCreatedAtDesc(@Param("adminUserId") Long adminUserId, Pageable pageable);
    
    // ==================== COUNT QUERIES ====================
    
    // Count by feedback
    long countByFeedback(Feedback feedback);
    long countByFeedbackId(Long feedbackId);
    
    // Count by admin user
    long countByAdminUser(User adminUser);
    long countByAdminUserId(Long adminUserId);
    
    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBefore(LocalDateTime date);
    
    // Count by feedback and admin user
    long countByFeedbackAndAdminUser(Feedback feedback, User adminUser);
    long countByFeedbackIdAndAdminUserId(Long feedbackId, Long adminUserId);
    
    // ==================== EXISTENCE QUERIES ====================
    
    // Check if reply exists by feedback
    boolean existsByFeedback(Feedback feedback);
    boolean existsByFeedbackId(Long feedbackId);
    
    // Check if reply exists by admin user
    boolean existsByAdminUser(User adminUser);
    boolean existsByAdminUserId(Long adminUserId);
    
    // Check if reply exists by feedback and admin user
    boolean existsByFeedbackAndAdminUser(Feedback feedback, User adminUser);
    boolean existsByFeedbackIdAndAdminUserId(Long feedbackId, Long adminUserId);
    
    // ==================== AGGREGATION QUERIES ====================
    
    // Get latest reply by feedback (business logic in repository)
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.feedback = :feedback ORDER BY fr.createdAt DESC")
    List<FeedbackReply> findLatestReplyByFeedback(@Param("feedback") Feedback feedback, Pageable pageable);
    
    // Get latest reply by feedback ID (business logic in repository)
    @Query("SELECT fr FROM FeedbackReply fr WHERE fr.feedback.id = :feedbackId ORDER BY fr.createdAt DESC")
    List<FeedbackReply> findLatestReplyByFeedbackId(@Param("feedbackId") Long feedbackId, Pageable pageable);
    
    // Get reply count by feedback
    @Query("SELECT f, COUNT(fr) FROM Feedback f LEFT JOIN FeedbackReply fr ON f.id = fr.feedback.id GROUP BY f.id")
    List<Object[]> getReplyCountByFeedback();
}