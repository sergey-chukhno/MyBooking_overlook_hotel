package com.MyBooking.event.repository;

import com.MyBooking.auth.domain.User;
import com.MyBooking.event.domain.Event;
import com.MyBooking.event.domain.EventBooking;
import com.MyBooking.event.domain.EventBookingStatus;
import com.MyBooking.reservation.domain.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for EventBooking entity operations.
 * Provides data access methods for event booking management, filtering, and search functionality.
 */
@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {

    @EntityGraph(attributePaths = {"event", "client", "reservation"})
    Optional<EventBooking> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {"event", "client", "reservation"})
    Optional<EventBooking> findByPublicIdAndClientId(UUID publicId, Long clientId);

    boolean existsByPublicId(UUID publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT eb FROM EventBooking eb WHERE eb.id = :id")
    Optional<EventBooking> findByIdForUpdate(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"event", "client", "reservation"})
    Page<EventBooking> findAll(Pageable pageable);

    // ==================== BASIC FINDER METHODS ====================
    
    /**
     * Find event bookings by event.
     * Used for filtering bookings by specific event.
     * 
     * @param event the event
     * @return list of bookings for the specified event
     */
    List<EventBooking> findByEvent(Event event);
    
    /**
     * Find event bookings by event ID.
     * Used for filtering bookings by event ID.
     * 
     * @param eventId the event ID
     * @return list of bookings for the specified event
     */
    List<EventBooking> findByEventId(Long eventId);
    
    /**
     * Find event bookings by event ID with pagination.
     * Used for event booking filtering with pagination.
     * 
     * @param eventId the event ID
     * @param pageable pagination information
     * @return page of bookings for the specified event
     */
    @EntityGraph(attributePaths = {"event", "client", "reservation"})
    Page<EventBooking> findByEventId(Long eventId, Pageable pageable);
    
    /**
     * Find event bookings by client.
     * Used for filtering bookings by specific client.
     * 
     * @param client the client
     * @return list of bookings for the specified client
     */
    List<EventBooking> findByClient(User client);
    
    /**
     * Find event bookings by client ID.
     * Used for filtering bookings by client ID.
     * 
     * @param clientId the client ID
     * @return list of bookings for the specified client
     */
    List<EventBooking> findByClientId(Long clientId);
    
    /**
     * Find event bookings by client ID with pagination.
     * Used for client booking filtering with pagination.
     * 
     * @param clientId the client ID
     * @param pageable pagination information
     * @return page of bookings for the specified client
     */
    @EntityGraph(attributePaths = {"event", "client", "reservation"})
    Page<EventBooking> findByClientId(Long clientId, Pageable pageable);
    
    /**
     * Find event bookings by reservation.
     * Used for filtering bookings by associated reservation.
     * 
     * @param reservation the reservation
     * @return list of bookings for the specified reservation
     */
    List<EventBooking> findByReservation(Reservation reservation);
    
    /**
     * Find event bookings by reservation ID.
     * Used for filtering bookings by reservation ID.
     * 
     * @param reservationId the reservation ID
     * @return list of bookings for the specified reservation
     */
    List<EventBooking> findByReservationId(Long reservationId);
    
    /**
     * Find event bookings by status.
     * Used for filtering bookings by booking status.
     * 
     * @param status the booking status
     * @return list of bookings with the specified status
     */
    List<EventBooking> findByStatus(EventBookingStatus status);
    
    /**
     * Find event bookings by status with pagination.
     * Used for status filtering with pagination.
     * 
     * @param status the booking status
     * @param pageable pagination information
     * @return page of bookings with the specified status
     */
    @EntityGraph(attributePaths = {"event", "client", "reservation"})
    Page<EventBooking> findByStatus(EventBookingStatus status, Pageable pageable);

    long countByEventIdAndStatus(Long eventId, EventBookingStatus status);

    // ==================== DATE AND TIME-BASED QUERIES ====================
    
    /**
     * Find event bookings by event date range.
     * Used for filtering bookings within an event date range.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of bookings with event dates in the range
     */
    @Query("SELECT eb FROM EventBooking eb WHERE eb.eventDateTime >= :startDate AND eb.eventDateTime <= :endDate")
    List<EventBooking> findByEventDateTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find event bookings by event date range with pagination.
     * Used for event date filtering with pagination.
     * 
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination information
     * @return page of bookings with event dates in the range
     */
    @Query("SELECT eb FROM EventBooking eb WHERE eb.eventDateTime >= :startDate AND eb.eventDateTime <= :endDate")
    Page<EventBooking> findByEventDateTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate, 
                                                 Pageable pageable);
    
    /**
     * Find event bookings by booking date range.
     * Used for filtering bookings within a booking date range.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of bookings with booking dates in the range
     */
    @Query("SELECT eb FROM EventBooking eb WHERE eb.bookingDate >= :startDate AND eb.bookingDate <= :endDate")
    List<EventBooking> findByBookingDateBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find event bookings by booking date range with pagination.
     * Used for booking date filtering with pagination.
     * 
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination information
     * @return page of bookings with booking dates in the range
     */
    @Query("SELECT eb FROM EventBooking eb WHERE eb.bookingDate >= :startDate AND eb.bookingDate <= :endDate")
    Page<EventBooking> findByBookingDateBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate, 
                                               Pageable pageable);
    
    /**
     * Find event bookings created between specified dates.
     * Used for time-based booking analytics.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of bookings created in the date range
     */
    @Query("SELECT eb FROM EventBooking eb WHERE eb.createdAt >= :startDate AND eb.createdAt <= :endDate")
    List<EventBooking> findBookingsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find event bookings updated between specified dates.
     * Used for time-based booking analytics.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of bookings updated in the date range
     */
    @Query("SELECT eb FROM EventBooking eb WHERE eb.updatedAt >= :startDate AND eb.updatedAt <= :endDate")
    List<EventBooking> findBookingsUpdatedBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);

    // ==================== PRICE-BASED QUERIES ====================
    
    /**
     * Find event bookings by total price range.
     * Used for filtering bookings within a price range.
     * 
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of bookings within the price range
     */
    List<EventBooking> findByTotalPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find event bookings by total price range with pagination.
     * Used for price range filtering with pagination.
     * 
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of bookings within the price range
     */
    Page<EventBooking> findByTotalPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Find event bookings with total price greater than or equal to specified value.
     * Used for filtering bookings above a minimum price.
     * 
     * @param minPrice minimum price
     * @return list of bookings above the minimum price
     */
    List<EventBooking> findByTotalPriceGreaterThanEqual(BigDecimal minPrice);
    
    /**
     * Find event bookings with total price less than or equal to specified value.
     * Used for filtering bookings below a maximum price.
     * 
     * @param maxPrice maximum price
     * @return list of bookings below the maximum price
     */
    List<EventBooking> findByTotalPriceLessThanEqual(BigDecimal maxPrice);

    // ==================== PARTICIPANT-BASED QUERIES ====================
    
    /**
     * Count total confirmed participants for an event.
     * Enforces aggregate participant summation invariant [INV-EVT-001].
     * 
     * @param eventId the event ID
     * @return total confirmed participants across all active bookings
     */
    @Query("SELECT COALESCE(SUM(eb.numberOfParticipants), 0) FROM EventBooking eb " +
           "WHERE eb.event.id = :eventId AND eb.status = 'CONFIRMED'")
    int countConfirmedParticipantsByEventId(@Param("eventId") Long eventId);
    
    /**
     * Find event bookings by number of participants range.
     * Used for filtering bookings within a participant range.
     * 
     * @param minParticipants minimum participants
     * @param maxParticipants maximum participants
     * @return list of bookings within the participant range
     */
    List<EventBooking> findByNumberOfParticipantsBetween(Integer minParticipants, Integer maxParticipants);
    
    /**
     * Find event bookings by number of participants range with pagination.
     * Used for participant range filtering with pagination.
     * 
     * @param minParticipants minimum participants
     * @param maxParticipants maximum participants
     * @param pageable pagination information
     * @return page of bookings within the participant range
     */
    Page<EventBooking> findByNumberOfParticipantsBetween(Integer minParticipants, Integer maxParticipants, Pageable pageable);
    
    /**
     * Find event bookings with participants greater than or equal to specified value.
     * Used for filtering bookings above a minimum participant count.
     * 
     * @param minParticipants minimum participants
     * @return list of bookings above the minimum participant count
     */
    List<EventBooking> findByNumberOfParticipantsGreaterThanEqual(Integer minParticipants);
    
    /**
     * Find event bookings with participants less than or equal to specified value.
     * Used for filtering bookings below a maximum participant count.
     * 
     * @param maxParticipants maximum participants
     * @return list of bookings below the maximum participant count
     */
    List<EventBooking> findByNumberOfParticipantsLessThanEqual(Integer maxParticipants);

    // ==================== DURATION-BASED QUERIES ====================
    
    /**
     * Find event bookings by duration range.
     * Used for filtering bookings within a duration range.
     * 
     * @param minDuration minimum duration in hours
     * @param maxDuration maximum duration in hours
     * @return list of bookings within the duration range
     */
    List<EventBooking> findByDurationHoursBetween(Integer minDuration, Integer maxDuration);
    
    /**
     * Find event bookings by duration range with pagination.
     * Used for duration range filtering with pagination.
     * 
     * @param minDuration minimum duration in hours
     * @param maxDuration maximum duration in hours
     * @param pageable pagination information
     * @return page of bookings within the duration range
     */
    Page<EventBooking> findByDurationHoursBetween(Integer minDuration, Integer maxDuration, Pageable pageable);
    
    /**
     * Find event bookings with duration greater than or equal to specified value.
     * Used for filtering bookings above a minimum duration.
     * 
     * @param minDuration minimum duration in hours
     * @return list of bookings above the minimum duration
     */
    List<EventBooking> findByDurationHoursGreaterThanEqual(Integer minDuration);
    
    /**
     * Find event bookings with duration less than or equal to specified value.
     * Used for filtering bookings below a maximum duration.
     * 
     * @param maxDuration maximum duration in hours
     * @return list of bookings below the maximum duration
     */
    List<EventBooking> findByDurationHoursLessThanEqual(Integer maxDuration);

    // ==================== COMBINED CRITERIA QUERIES ====================
    
    /**
     * Find event bookings by event and status.
     * Used for filtering bookings by event and status.
     * 
     * @param eventId the event ID
     * @param status the booking status
     * @return list of bookings matching the criteria
     */
    @Query("SELECT eb FROM EventBooking eb WHERE eb.event.id = :eventId AND eb.status = :status")
    List<EventBooking> findByEventIdAndStatus(@Param("eventId") Long eventId, 
                                             @Param("status") EventBookingStatus status);
    
    /**
     * Find event bookings by client and status.
     * Used for filtering bookings by client and status.
     * 
     * @param clientId the client ID
     * @param status the booking status
     * @return list of bookings matching the criteria
     */
    @Query("SELECT eb FROM EventBooking eb WHERE eb.client.id = :clientId AND eb.status = :status")
    List<EventBooking> findByClientIdAndStatus(@Param("clientId") Long clientId, 
                                              @Param("status") EventBookingStatus status);
    
    /**
     * Find event bookings by event date and status.
     * Used for filtering bookings by event date and status.
     * 
     * @param eventDate the event date
     * @param status the booking status
     * @return list of bookings matching the criteria
     */
    @Query("SELECT eb FROM EventBooking eb WHERE DATE(eb.eventDateTime) = DATE(:eventDate) AND eb.status = :status")
    List<EventBooking> findByEventDateAndStatus(@Param("eventDate") LocalDateTime eventDate, 
                                               @Param("status") EventBookingStatus status);

    // ==================== STATISTICS AND ANALYTICS ====================
    
    /**
     * Count event bookings by status.
     * Used for booking status statistics.
     * 
     * @param status the booking status
     * @return count of bookings with the specified status
     */
    @Query("SELECT COUNT(eb) FROM EventBooking eb WHERE eb.status = :status")
    Long countByStatus(@Param("status") EventBookingStatus status);
    
    /**
     * Count event bookings by event.
     * Used for event booking statistics.
     * 
     * @param eventId the event ID
     * @return count of bookings for the specified event
     */
    @Query("SELECT COUNT(eb) FROM EventBooking eb WHERE eb.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);
    
    /**
     * Count event bookings by client.
     * Used for client booking statistics.
     * 
     * @param clientId the client ID
     * @return count of bookings for the specified client
     */
    @Query("SELECT COUNT(eb) FROM EventBooking eb WHERE eb.client.id = :clientId")
    Long countByClientId(@Param("clientId") Long clientId);
    
    /**
     * Get average total price by status.
     * Used for price analytics by booking status.
     * 
     * @param status the booking status
     * @return average total price for the status
     */
    @Query("SELECT AVG(eb.totalPrice) FROM EventBooking eb WHERE eb.status = :status")
    Optional<BigDecimal> getAverageTotalPriceByStatus(@Param("status") EventBookingStatus status);
    
    /**
     * Get average number of participants by status.
     * Used for participant analytics by booking status.
     * 
     * @param status the booking status
     * @return average number of participants for the status
     */
    @Query("SELECT AVG(eb.numberOfParticipants) FROM EventBooking eb WHERE eb.status = :status")
    Optional<Double> getAverageParticipantsByStatus(@Param("status") EventBookingStatus status);
    
    /**
     * Get total revenue by status.
     * Used for revenue analytics by booking status.
     * 
     * @param status the booking status
     * @return total revenue for the status
     */
    @Query("SELECT SUM(eb.totalPrice) FROM EventBooking eb WHERE eb.status = :status")
    Optional<BigDecimal> getTotalRevenueByStatus(@Param("status") EventBookingStatus status);

    // ==================== EXISTENCE CHECKS ====================
    
    /**
     * Check if event bookings exist by event.
     * Used for validation and business logic.
     * 
     * @param eventId the event ID
     * @return true if bookings exist for the specified event
     */
    boolean existsByEventId(Long eventId);
    
    /**
     * Check if event bookings exist by client.
     * Used for validation and business logic.
     * 
     * @param clientId the client ID
     * @return true if bookings exist for the specified client
     */
    boolean existsByClientId(Long clientId);
    
    /**
     * Check if event bookings exist by status.
     * Used for validation and business logic.
     * 
     * @param status the booking status
     * @return true if bookings exist with the specified status
     */
    boolean existsByStatus(EventBookingStatus status);

    // ==================== ADVANCED SEARCH WITH MULTIPLE CRITERIA ====================
    
    /**
     * Find event bookings by multiple criteria.
     * Used for advanced booking search functionality.
     * 
     * @param eventId event ID (optional)
     * @param clientId client ID (optional)
     * @param status booking status (optional)
     * @param minPrice minimum total price (optional)
     * @param maxPrice maximum total price (optional)
     * @param minParticipants minimum participants (optional)
     * @param maxParticipants maximum participants (optional)
     * @param minDuration minimum duration (optional)
     * @param maxDuration maximum duration (optional)
     * @return list of bookings matching the criteria
     */
    @EntityGraph(attributePaths = {"event", "client", "reservation"})
    @Query("SELECT eb FROM EventBooking eb WHERE " +
           "(:eventId IS NULL OR eb.event.id = :eventId) AND " +
           "(:clientId IS NULL OR eb.client.id = :clientId) AND " +
           "(:status IS NULL OR eb.status = :status) AND " +
           "(:minPrice IS NULL OR eb.totalPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR eb.totalPrice <= :maxPrice) AND " +
           "(:minParticipants IS NULL OR eb.numberOfParticipants >= :minParticipants) AND " +
           "(:maxParticipants IS NULL OR eb.numberOfParticipants <= :maxParticipants) AND " +
           "(:minDuration IS NULL OR eb.durationHours >= :minDuration) AND " +
           "(:maxDuration IS NULL OR eb.durationHours <= :maxDuration)")
    List<EventBooking> findByCriteria(@Param("eventId") Long eventId,
                                     @Param("clientId") Long clientId,
                                     @Param("status") EventBookingStatus status,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     @Param("minParticipants") Integer minParticipants,
                                     @Param("maxParticipants") Integer maxParticipants,
                                     @Param("minDuration") Integer minDuration,
                                     @Param("maxDuration") Integer maxDuration);
    
    /**
     * Find event bookings by multiple criteria with pagination.
     * Used for advanced booking search with pagination.
     * 
     * @param eventId event ID (optional)
     * @param clientId client ID (optional)
     * @param status booking status (optional)
     * @param minPrice minimum total price (optional)
     * @param maxPrice maximum total price (optional)
     * @param minParticipants minimum participants (optional)
     * @param maxParticipants maximum participants (optional)
     * @param minDuration minimum duration (optional)
     * @param maxDuration maximum duration (optional)
     * @param pageable pagination information
     * @return page of bookings matching the criteria
     */
    @EntityGraph(attributePaths = {"event", "client", "reservation"})
    @Query("SELECT eb FROM EventBooking eb WHERE " +
           "(:eventId IS NULL OR eb.event.id = :eventId) AND " +
           "(:clientId IS NULL OR eb.client.id = :clientId) AND " +
           "(:status IS NULL OR eb.status = :status) AND " +
           "(:minPrice IS NULL OR eb.totalPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR eb.totalPrice <= :maxPrice) AND " +
           "(:minParticipants IS NULL OR eb.numberOfParticipants >= :minParticipants) AND " +
           "(:maxParticipants IS NULL OR eb.numberOfParticipants <= :maxParticipants) AND " +
           "(:minDuration IS NULL OR eb.durationHours >= :minDuration) AND " +
           "(:maxDuration IS NULL OR eb.durationHours <= :maxDuration)")
    Page<EventBooking> findByCriteria(@Param("eventId") Long eventId,
                                     @Param("clientId") Long clientId,
                                     @Param("status") EventBookingStatus status,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     @Param("minParticipants") Integer minParticipants,
                                     @Param("maxParticipants") Integer maxParticipants,
                                     @Param("minDuration") Integer minDuration,
                                     @Param("maxDuration") Integer maxDuration,
                                     Pageable pageable);

    /**
     * Calculate total revenue from confirmed event bookings within the specified date/time range.
     * Uses database push-down SUM aggregation, replacing hardcoded mock numbers (AUD-ANL-002, INV-ANL-002).
     * 
     * @param startDateTime The start of the reporting window (inclusive)
     * @param endDateTime The end of the reporting window (inclusive)
     * @param status The booking status (typically CONFIRMED)
     * @return Sum of total prices or 0.00
     */
    @Query("SELECT COALESCE(SUM(eb.totalPrice), 0) FROM EventBooking eb " +
           "WHERE eb.status = :status " +
           "AND eb.eventDateTime >= :startDateTime " +
           "AND eb.eventDateTime <= :endDateTime")
    BigDecimal calculateTotalRevenueBetweenDates(@Param("startDateTime") LocalDateTime startDateTime,
                                                 @Param("endDateTime") LocalDateTime endDateTime,
                                                 @Param("status") EventBookingStatus status);
}