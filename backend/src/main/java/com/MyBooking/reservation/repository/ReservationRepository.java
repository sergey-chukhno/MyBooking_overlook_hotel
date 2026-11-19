package com.MyBooking.reservation.repository;

import com.MyBooking.reservation.domain.Reservation;
import com.MyBooking.reservation.domain.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"client", "room"})
    Optional<Reservation> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {"client", "room"})
    Optional<Reservation> findByPublicIdAndClientId(UUID publicId, Long clientId);

    boolean existsByPublicId(UUID publicId);

    @Override
    @EntityGraph(attributePaths = {"client", "room"})
    Page<Reservation> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    // ==================== CLIENT-BASED QUERIES ====================
    
    /**
     * Find all reservations for a specific client.
     * 
     * @param clientId The client's ID
     * @return List of reservations for the client
     */
    List<Reservation> findByClientId(Long clientId);
    
    /**
     * Find all reservations for a specific client with pagination.
     * 
     * @param clientId The client's ID
     * @param pageable Pagination information
     * @return Page of reservations for the client
     */
    @EntityGraph(attributePaths = {"client", "room"})
    Page<Reservation> findByClientId(Long clientId, Pageable pageable);
    
    /**
     * Find all reservations for a specific client and status.
     * 
     * @param clientId The client's ID
     * @param status The reservation status
     * @return List of reservations matching the criteria
     */
    List<Reservation> findByClientIdAndStatus(Long clientId, ReservationStatus status);
    
    /**
     * Count reservations for a specific client.
     * 
     * @param clientId The client's ID
     * @return Number of reservations for the client
     */
    long countByClientId(Long clientId);
    
    /**
     * Count reservations for a specific client and status.
     * 
     * @param clientId The client's ID
     * @param status The reservation status
     * @return Number of reservations matching the criteria
     */
    long countByClientIdAndStatus(Long clientId, ReservationStatus status);

    // ==================== ROOM-BASED QUERIES ====================
    
    /**
     * Find all reservations for a specific room.
     * 
     * @param roomId The room's ID
     * @return List of reservations for the room
     */
    List<Reservation> findByRoomId(Long roomId);
    
    /**
     * Find all reservations for a specific room with pagination.
     * 
     * @param roomId The room's ID
     * @param pageable Pagination information
     * @return Page of reservations for the room
     */
    @EntityGraph(attributePaths = {"client", "room"})
    Page<Reservation> findByRoomId(Long roomId, Pageable pageable);
    
    /**
     * Find all reservations for a specific room and status.
     * 
     * @param roomId The room's ID
     * @param status The reservation status
     * @return List of reservations matching the criteria
     */
    List<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatus status);
    
    /**
     * Count reservations for a specific room.
     * 
     * @param roomId The room's ID
     * @return Number of reservations for the room
     */
    long countByRoomId(Long roomId);
    
    /**
     * Count reservations for a specific room and status.
     * 
     * @param roomId The room's ID
     * @param status The reservation status
     * @return Number of reservations matching the criteria
     */
    long countByRoomIdAndStatus(Long roomId, ReservationStatus status);

    boolean existsByRoomIdAndStatusAndCheckOutGreaterThanEqual(Long roomId, ReservationStatus status, LocalDate date);

    // ==================== STATUS-BASED QUERIES ====================
    
    /**
     * Find all reservations with a specific status.
     * 
     * @param status The reservation status
     * @return List of reservations with the status
     */
    List<Reservation> findByStatus(ReservationStatus status);
    
    /**
     * Find all reservations with a specific status and pagination.
     * 
     * @param status The reservation status
     * @param pageable Pagination information
     * @return Page of reservations with the status
     */
    @EntityGraph(attributePaths = {"client", "room"})
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);
    
    /**
     * Count reservations with a specific status.
     * 
     * @param status The reservation status
     * @return Number of reservations with the status
     */
    long countByStatus(ReservationStatus status);

    // ==================== DATE-BASED QUERIES ====================
    
    /**
     * Find reservations with check-in date between the given dates.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of reservations with check-in in the date range
     */
    List<Reservation> findByCheckInBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find reservations with check-out date between the given dates.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of reservations with check-out in the date range
     */
    List<Reservation> findByCheckOutBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find reservations with check-in date between the given dates and pagination.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination information
     * @return Page of reservations with check-in in the date range
     */
    @EntityGraph(attributePaths = {"client", "room"})
    Page<Reservation> findByCheckInBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find reservations with check-out date between the given dates and pagination.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination information
     * @return Page of reservations with check-out in the date range
     */
    Page<Reservation> findByCheckOutBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find reservations with check-in date on or after the given date.
     * 
     * @param date The date
     * @return List of reservations with check-in on or after the date
     */
    List<Reservation> findByCheckInGreaterThanEqual(LocalDate date);
    
    /**
     * Find reservations with check-out date on or before the given date.
     * 
     * @param date The date
     * @return List of reservations with check-out on or before the date
     */
    List<Reservation> findByCheckOutLessThanEqual(LocalDate date);
    
    /**
     * Find active reservations (where today is between check-in and check-out dates).
     * 
     * @param checkInDate Check-in date (today)
     * @param checkOutDate Check-out date (today)
     * @return List of active reservations
     */
    List<Reservation> findByCheckInLessThanEqualAndCheckOutGreaterThanEqual(LocalDate checkInDate, LocalDate checkOutDate);

    // ==================== AVAILABILITY QUERIES ====================
    
    /**
     * Check if a room is available for the given date range, optionally excluding a reservation ID.
     * Returns conflicting reservations if any exist.
     * 
     * @param roomId The room to check
     * @param checkIn Start date of the requested period
     * @param checkOut End date of the requested period
     * @param excludeReservationId Optional reservation ID to exclude (e.g., during update)
     * @return List of conflicting reservations (empty if room is available)
     */
    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId " +
           "AND r.status = 'CONFIRMED' " +
           "AND (:excludeReservationId IS NULL OR r.id != :excludeReservationId) " +
           "AND (r.checkIn < :checkOut AND r.checkOut > :checkIn)")
    List<Reservation> checkRoomAvailability(@Param("roomId") Long roomId,
                                           @Param("checkIn") LocalDate checkIn,
                                           @Param("checkOut") LocalDate checkOut,
                                           @Param("excludeReservationId") Long excludeReservationId);

    /**
     * Check if a room is available for the given date range (convenience overload).
     * 
     * @param roomId The room to check
     * @param checkIn Start date of the requested period
     * @param checkOut End date of the requested period
     * @return List of conflicting reservations (empty if room is available)
     */
    default List<Reservation> checkRoomAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return checkRoomAvailability(roomId, checkIn, checkOut, null);
    }

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.room.id = :roomId " +
           "AND r.status = 'CONFIRMED' " +
           "AND (:excludeReservationId IS NULL OR r.id != :excludeReservationId) " +
           "AND (r.checkIn < :checkOut AND r.checkOut > :checkIn)")
    long countConflictingReservations(@Param("roomId") Long roomId,
                                      @Param("checkIn") LocalDate checkIn,
                                      @Param("checkOut") LocalDate checkOut,
                                      @Param("excludeReservationId") Long excludeReservationId);

    default long countConflictingReservations(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return countConflictingReservations(roomId, checkIn, checkOut, null);
    }
    
    /**
     * Find all reservations that overlap with the given date range.
     * 
     * @param checkIn Start date of the period
     * @param checkOut End date of the period
     * @return List of overlapping reservations
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' " +
           "AND (r.checkIn < :checkOut AND r.checkOut > :checkIn)")
    List<Reservation> findOverlappingReservations(@Param("checkIn") LocalDate checkIn,
                                                  @Param("checkOut") LocalDate checkOut);
    
    /**
     * Find all reservations that overlap with the given date range and pagination.
     * 
     * @param checkIn Start date of the period
     * @param checkOut End date of the period
     * @param pageable Pagination information
     * @return Page of overlapping reservations
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' " +
           "AND (r.checkIn < :checkOut AND r.checkOut > :checkIn)")
    Page<Reservation> findOverlappingReservations(@Param("checkIn") LocalDate checkIn,
                                                  @Param("checkOut") LocalDate checkOut,
                                                  Pageable pageable);

    // ==================== PRICE-BASED QUERIES ====================
    
    /**
     * Find reservations with total price between the given values.
     * 
     * @param minPrice Minimum price (inclusive)
     * @param maxPrice Maximum price (inclusive)
     * @return List of reservations with price in the range
     */
    List<Reservation> findByTotalPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find reservations with total price between the given values and pagination.
     * 
     * @param minPrice Minimum price (inclusive)
     * @param maxPrice Maximum price (inclusive)
     * @param pageable Pagination information
     * @return Page of reservations with price in the range
     */
    Page<Reservation> findByTotalPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Find reservations with a specific currency.
     * 
     * @param currency The currency code
     * @return List of reservations with the currency
     */
    List<Reservation> findByCurrency(String currency);
    
    /**
     * Find reservations with a specific currency and pagination.
     * 
     * @param currency The currency code
     * @param pageable Pagination information
     * @return Page of reservations with the currency
     */
    Page<Reservation> findByCurrency(String currency, Pageable pageable);
    
    /**
     * Find reservations with total price greater than or equal to the given value.
     * 
     * @param minPrice Minimum price (inclusive)
     * @return List of reservations with price >= minPrice
     */
    List<Reservation> findByTotalPriceGreaterThanEqual(BigDecimal minPrice);
    
    /**
     * Find reservations with total price less than or equal to the given value.
     * 
     * @param maxPrice Maximum price (inclusive)
     * @return List of reservations with price <= maxPrice
     */
    List<Reservation> findByTotalPriceLessThanEqual(BigDecimal maxPrice);

    // ==================== GUEST COUNT QUERIES ====================
    
    /**
     * Find reservations with a specific number of guests.
     * 
     * @param numberOfGuests The number of guests
     * @return List of reservations with the guest count
     */
    List<Reservation> findByNumberOfGuests(Integer numberOfGuests);
    
    /**
     * Find reservations with number of guests between the given values.
     * 
     * @param minGuests Minimum number of guests (inclusive)
     * @param maxGuests Maximum number of guests (inclusive)
     * @return List of reservations with guest count in the range
     */
    List<Reservation> findByNumberOfGuestsBetween(Integer minGuests, Integer maxGuests);
    
    /**
     * Find reservations with number of guests greater than or equal to the given value.
     * 
     * @param minGuests Minimum number of guests (inclusive)
     * @return List of reservations with guest count >= minGuests
     */
    List<Reservation> findByNumberOfGuestsGreaterThanEqual(Integer minGuests);
    
    /**
     * Find reservations with number of guests less than or equal to the given value.
     * 
     * @param maxGuests Maximum number of guests (inclusive)
     * @return List of reservations with guest count <= maxGuests
     */
    List<Reservation> findByNumberOfGuestsLessThanEqual(Integer maxGuests);

    // ==================== COMBINED CRITERIA QUERIES ====================
    
    /**
     * Find reservations by multiple criteria with pagination.
     * 
     * @param clientId Optional client ID filter
     * @param roomId Optional room ID filter
     * @param status Optional status filter
     * @param minPrice Optional minimum price filter
     * @param maxPrice Optional maximum price filter
     * @param currency Optional currency filter
     * @param minGuests Optional minimum guests filter
     * @param maxGuests Optional maximum guests filter
     * @param checkInFrom Optional check-in date from filter
     * @param checkInTo Optional check-in date to filter
     * @param pageable Pagination information
     * @return Page of reservations matching the criteria
     */
    @EntityGraph(attributePaths = {"client", "room"})
    @Query("SELECT r FROM Reservation r WHERE " +
           "(:clientId IS NULL OR r.client.id = :clientId) AND " +
           "(:roomId IS NULL OR r.room.id = :roomId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:minPrice IS NULL OR r.totalPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR r.totalPrice <= :maxPrice) AND " +
           "(:currency IS NULL OR r.currency = :currency) AND " +
           "(:minGuests IS NULL OR r.numberOfGuests >= :minGuests) AND " +
           "(:maxGuests IS NULL OR r.numberOfGuests <= :maxGuests) AND " +
           "(:checkInFrom IS NULL OR r.checkIn >= :checkInFrom) AND " +
           "(:checkInTo IS NULL OR r.checkIn <= :checkInTo)")
    Page<Reservation> findByCriteria(@Param("clientId") Long clientId,
                                     @Param("roomId") Long roomId,
                                     @Param("status") ReservationStatus status,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     @Param("currency") String currency,
                                     @Param("minGuests") Integer minGuests,
                                     @Param("maxGuests") Integer maxGuests,
                                     @Param("checkInFrom") LocalDate checkInFrom,
                                     @Param("checkInTo") LocalDate checkInTo,
                                     Pageable pageable);

    /**
     * Alternative search method with better null handling for date parameters
     */
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.client.id = :clientId AND " +
           "(:roomId IS NULL OR r.room.id = :roomId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:checkInFrom IS NULL OR r.checkIn >= :checkInFrom) AND " +
           "(:checkInTo IS NULL OR r.checkIn <= :checkInTo)")
    Page<Reservation> findByClientAndDateRange(@Param("clientId") Long clientId,
                                               @Param("roomId") Long roomId,
                                               @Param("status") ReservationStatus status,
                                               @Param("checkInFrom") LocalDate checkInFrom,
                                               @Param("checkInTo") LocalDate checkInTo,
                                               Pageable pageable);
    
    /**
     * Search method for client reservations with date range - handles null dates properly
     */
    @EntityGraph(attributePaths = {"client", "room"})
    @Query("SELECT r FROM Reservation r WHERE r.client.id = :clientId AND r.checkIn >= :checkInFrom AND r.checkIn <= :checkInTo")
    Page<Reservation> findByClientIdAndDateRange(@Param("clientId") Long clientId,
                                                 @Param("checkInFrom") LocalDate checkInFrom,
                                                 @Param("checkInTo") LocalDate checkInTo,
                                                 Pageable pageable);

    // ==================== EXISTENCE CHECKS ====================
    
    /**
     * Check if a reservation exists for a specific client, room, and status.
     * 
     * @param clientId The client's ID
     * @param roomId The room's ID
     * @param status The reservation status
     * @return true if reservation exists, false otherwise
     */
    boolean existsByClientIdAndRoomIdAndStatus(Long clientId, Long roomId, ReservationStatus status);
    
    /**
     * Check if a reservation exists for a specific client and room.
     * 
     * @param clientId The client's ID
     * @param roomId The room's ID
     * @return true if reservation exists, false otherwise
     */
    boolean existsByClientIdAndRoomId(Long clientId, Long roomId);
    
    /**
     * Check if a reservation exists for a specific room and status.
     * 
     * @param roomId The room's ID
     * @param status The reservation status
     * @return true if reservation exists, false otherwise
     */
    boolean existsByRoomIdAndStatus(Long roomId, ReservationStatus status);

    // ==================== TIME-BASED QUERIES ====================
    
    /**
     * Find reservations created between the given dates.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of reservations created in the date range
     */
    List<Reservation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find reservations updated between the given dates.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of reservations updated in the date range
     */
    List<Reservation> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find reservations created after the given date.
     * 
     * @param date The date
     * @return List of reservations created after the date
     */
    List<Reservation> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find reservations updated after the given date.
     * 
     * @param date The date
     * @return List of reservations updated after the date
     */
    List<Reservation> findByUpdatedAtAfter(LocalDateTime date);

    // ==================== STATISTICS QUERIES ====================
    
    /**
     * Get the total revenue from confirmed reservations.
     * 
     * @return Total revenue as BigDecimal
     */
    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r WHERE r.status = 'CONFIRMED'")
    BigDecimal getTotalRevenue();
    
    /**
     * Get the total revenue from confirmed reservations for a specific currency.
     * 
     * @param currency The currency code
     * @return Total revenue as BigDecimal
     */
    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r WHERE r.status = 'CONFIRMED' AND r.currency = :currency")
    BigDecimal getTotalRevenueByCurrency(@Param("currency") String currency);
    
    /**
     * Get the average reservation price.
     * 
     * @return Average price as BigDecimal
     */
    @Query("SELECT COALESCE(AVG(r.totalPrice), 0) FROM Reservation r WHERE r.status = 'CONFIRMED'")
    BigDecimal getAverageReservationPrice();
    
    /**
     * Get the average reservation price for a specific currency.
     * 
     * @param currency The currency code
     * @return Average price as BigDecimal
     */
    @Query("SELECT COALESCE(AVG(r.totalPrice), 0) FROM Reservation r WHERE r.status = 'CONFIRMED' AND r.currency = :currency")
    BigDecimal getAverageReservationPriceByCurrency(@Param("currency") String currency);

    /**
     * Find confirmed reservations overlapping with a specific calendar date range for analytics reporting.
     * A reservation overlaps with [startDate, endDate] if its checkIn is on or before endDate
     * and its checkOut is strictly after startDate.
     * 
     * @param startDate The query window start date (inclusive)
     * @param endDate The query window end date (inclusive)
     * @param status The reservation status
     * @return List of overlapping reservations
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.status = :status " +
           "AND r.checkIn <= :endDate " +
           "AND r.checkOut > :startDate")
    List<Reservation> findOverlappingReservationsInDateRange(@Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate,
                                                            @Param("status") ReservationStatus status);
}