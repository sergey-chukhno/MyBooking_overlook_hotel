package com.MyBooking.feedback.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.MyBooking.feedback.domain.Feedback;
import com.MyBooking.reservation.domain.Reservation;
import com.MyBooking.auth.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    // ==================== BASIC QUERIES ====================

    @EntityGraph(attributePaths = {"user", "reservation"})
    Optional<Feedback> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {"user", "reservation"})
    Optional<Feedback> findByPublicIdAndUserId(UUID publicId, Long userId);

    boolean existsByPublicId(UUID publicId);

    @Override
    @EntityGraph(attributePaths = {"user", "reservation"})
    Page<Feedback> findAll(Pageable pageable);

    // Find by user (entity-based)
    List<Feedback> findByUser(User user);
    @EntityGraph(attributePaths = {"user", "reservation"})
    Page<Feedback> findByUser(User user, Pageable pageable);
    
    // Find by user (ID-based)
    List<Feedback> findByUserId(Long userId);
    @EntityGraph(attributePaths = {"user", "reservation"})
    Page<Feedback> findByUserId(Long userId, Pageable pageable);
    
    // Find by reservation (entity-based)
    List<Feedback> findByReservation(Reservation reservation);
    @EntityGraph(attributePaths = {"user", "reservation"})
    Page<Feedback> findByReservation(Reservation reservation, Pageable pageable);
    
    // Find by reservation (ID-based)
    List<Feedback> findByReservationId(Long reservationId);
    @EntityGraph(attributePaths = {"user", "reservation"})
    Page<Feedback> findByReservationId(Long reservationId, Pageable pageable);
    
    // Find by rating
    List<Feedback> findByRating(Integer rating);
    Page<Feedback> findByRating(Integer rating, Pageable pageable);
    
    // Find by rating range
    List<Feedback> findByRatingBetween(Integer minRating, Integer maxRating);
    @EntityGraph(attributePaths = {"user", "reservation"})
    Page<Feedback> findByRatingBetween(Integer minRating, Integer maxRating, Pageable pageable);
    
    // Find by user and reservation (unique constraint)
    Optional<Feedback> findByUserAndReservation(User user, Reservation reservation);
    Optional<Feedback> findByUserIdAndReservationId(Long userId, Long reservationId);
    
    // ==================== DATE-BASED QUERIES ====================
    
    // Find by creation date range
    List<Feedback> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Feedback> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by creation date after
    List<Feedback> findByCreatedAtAfter(LocalDateTime date);
    @EntityGraph(attributePaths = {"user", "reservation"})
    Page<Feedback> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);
    
    // Find by creation date before
    List<Feedback> findByCreatedAtBefore(LocalDateTime date);
    Page<Feedback> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);
    
    // ==================== COMBINED QUERIES ====================
    
    // Find by reservation and rating
    List<Feedback> findByReservationAndRating(Reservation reservation, Integer rating);
    Page<Feedback> findByReservationAndRating(Reservation reservation, Integer rating, Pageable pageable);
    List<Feedback> findByReservationIdAndRating(Long reservationId, Integer rating);
    Page<Feedback> findByReservationIdAndRating(Long reservationId, Integer rating, Pageable pageable);
    
    // Find by user and rating
    List<Feedback> findByUserAndRating(User user, Integer rating);
    Page<Feedback> findByUserAndRating(User user, Integer rating, Pageable pageable);
    List<Feedback> findByUserIdAndRating(Long userId, Integer rating);
    Page<Feedback> findByUserIdAndRating(Long userId, Integer rating, Pageable pageable);
    
    // Find by rating greater than or equal
    List<Feedback> findByRatingGreaterThanEqual(Integer minRating);
    Page<Feedback> findByRatingGreaterThanEqual(Integer minRating, Pageable pageable);
    
    // Find by rating less than or equal
    List<Feedback> findByRatingLessThanEqual(Integer maxRating);
    Page<Feedback> findByRatingLessThanEqual(Integer maxRating, Pageable pageable);
    
    // ==================== CUSTOM BUSINESS QUERIES ====================
    
    // Find recent feedbacks (business logic in repository - following current pattern)
    @Query("SELECT f FROM Feedback f WHERE f.createdAt >= :since ORDER BY f.createdAt DESC")
    List<Feedback> findRecentFeedbacks(@Param("since") LocalDateTime since);
    
    @EntityGraph(attributePaths = {"user", "reservation"})
    @Query("SELECT f FROM Feedback f WHERE f.createdAt >= :since ORDER BY f.createdAt DESC")
    Page<Feedback> findRecentFeedbacks(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Find feedbacks with comments (business logic in repository - following current pattern)
    @Query("SELECT f FROM Feedback f WHERE f.comment IS NOT NULL AND f.comment != ''")
    List<Feedback> findFeedbacksWithComments();
    
    @EntityGraph(attributePaths = {"user", "reservation"})
    @Query("SELECT f FROM Feedback f WHERE f.comment IS NOT NULL AND f.comment != ''")
    Page<Feedback> findFeedbacksWithComments(Pageable pageable);
    
    // Find feedbacks without comments (business logic in repository - following current pattern)
    @Query("SELECT f FROM Feedback f WHERE f.comment IS NULL OR f.comment = ''")
    List<Feedback> findFeedbacksWithoutComments();
    
    @EntityGraph(attributePaths = {"user", "reservation"})
    @Query("SELECT f FROM Feedback f WHERE f.comment IS NULL OR f.comment = ''")
    Page<Feedback> findFeedbacksWithoutComments(Pageable pageable);
    
    // Find high-rated feedbacks (business logic in repository - following current pattern)
    @Query("SELECT f FROM Feedback f WHERE f.rating >= 4 ORDER BY f.rating DESC, f.createdAt DESC")
    List<Feedback> findHighRatedFeedbacks();
    
    @EntityGraph(attributePaths = {"user", "reservation"})
    @Query("SELECT f FROM Feedback f WHERE f.rating >= 4 ORDER BY f.rating DESC, f.createdAt DESC")
    Page<Feedback> findHighRatedFeedbacks(Pageable pageable);
    
    // Find low-rated feedbacks (business logic in repository - following current pattern)
    @Query("SELECT f FROM Feedback f WHERE f.rating <= 2 ORDER BY f.rating ASC, f.createdAt DESC")
    List<Feedback> findLowRatedFeedbacks();
    
    @EntityGraph(attributePaths = {"user", "reservation"})
    @Query("SELECT f FROM Feedback f WHERE f.rating <= 2 ORDER BY f.rating ASC, f.createdAt DESC")
    Page<Feedback> findLowRatedFeedbacks(Pageable pageable);
    
    // ==================== COUNT QUERIES ====================
    
    // Count by user
    long countByUser(User user);
    long countByUserId(Long userId);
    
    // Count by reservation
    long countByReservation(Reservation reservation);
    long countByReservationId(Long reservationId);
    
    // Count by rating
    long countByRating(Integer rating);
    long countByRatingBetween(Integer minRating, Integer maxRating);
    
    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBefore(LocalDateTime date);
    
    // Count by user and rating
    long countByUserAndRating(User user, Integer rating);
    long countByUserIdAndRating(Long userId, Integer rating);
    
    // Count by reservation and rating
    long countByReservationAndRating(Reservation reservation, Integer rating);
    long countByReservationIdAndRating(Long reservationId, Integer rating);
    
    // ==================== EXISTENCE QUERIES ====================
    
    // Check if feedback exists by user and reservation
    boolean existsByUserAndReservation(User user, Reservation reservation);
    boolean existsByUserIdAndReservationId(Long userId, Long reservationId);
    
    // Check if feedback exists by user
    boolean existsByUser(User user);
    boolean existsByUserId(Long userId);
    
    // Check if feedback exists by reservation
    boolean existsByReservation(Reservation reservation);
    boolean existsByReservationId(Long reservationId);
    
    // Check if feedback exists by rating
    boolean existsByRating(Integer rating);
    
    // ==================== AGGREGATION QUERIES ====================
    
    // Get average rating by reservation
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.reservation = :reservation")
    Double getAverageRatingByReservation(@Param("reservation") Reservation reservation);
    
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.reservation.id = :reservationId")
    Double getAverageRatingByReservationId(@Param("reservationId") Long reservationId);
    
    // Get average rating by user
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.user = :user")
    Double getAverageRatingByUser(@Param("user") User user);
    
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.user.id = :userId")
    Double getAverageRatingByUserId(@Param("userId") Long userId);
    
    // Get average rating overall
    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double getAverageRatingOverall();
    
    // Get rating distribution
    @Query("SELECT f.rating, COUNT(f) FROM Feedback f GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> getRatingDistribution();

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.comment IS NOT NULL AND TRIM(f.comment) <> ''")
    long countFeedbacksWithComments();
}