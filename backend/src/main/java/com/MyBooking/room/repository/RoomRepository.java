package com.MyBooking.room.repository;

import com.MyBooking.room.domain.Room;
import com.MyBooking.room.domain.RoomStatus;
import com.MyBooking.room.domain.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

/**
 * Repository interface for Room entity operations.
 * Provides data access methods for room management, availability, and search functionality.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    /**
     * Find room by room number.
     * Used for room lookup and validation.
     * 
     * @param number the room number
     * @return Optional containing the room if found
     */
    Optional<Room> findByNumber(String number);

    /**
     * Check if a room exists with the given number.
     * Used for room number validation during creation.
     * 
     * @param number the room number
     * @return true if room exists, false otherwise
     */
    boolean existsByNumber(String number);

    /**
     * Find rooms by room type.
     * Used for filtering rooms by category.
     * 
     * @param roomType the room type
     * @return list of rooms with the specified type
     */
    List<Room> findByRoomType(RoomType roomType);

    /**
     * Find rooms by room type with pagination.
     * Used for room type filtering with pagination.
     * 
     * @param roomType the room type
     * @param pageable pagination information
     * @return page of rooms with the specified type
     */
    Page<Room> findByRoomType(RoomType roomType, Pageable pageable);

    /**
     * Find rooms by status.
     * Used for filtering rooms by availability status.
     * 
     * @param status the room status
     * @return list of rooms with the specified status
     */
    List<Room> findByStatus(RoomStatus status);

    /**
     * Find rooms by status with pagination.
     * Used for status filtering with pagination.
     * 
     * @param status the room status
     * @param pageable pagination information
     * @return page of rooms with the specified status
     */
    Page<Room> findByStatus(RoomStatus status, Pageable pageable);


    /**
     * Find rooms by capacity (minimum capacity).
     * Used for filtering rooms that can accommodate a certain number of guests.
     * 
     * @param capacity the minimum capacity required
     * @return list of rooms with capacity greater than or equal to the specified value
     */
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);

    /**
     * Find rooms by capacity range.
     * Used for filtering rooms within a specific capacity range.
     * 
     * @param minCapacity minimum capacity
     * @param maxCapacity maximum capacity
     * @return list of rooms within the capacity range
     */
    List<Room> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);

    /**
     * Find rooms by price range.
     * Used for filtering rooms within a specific price range.
     * 
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of rooms within the price range
     */
    List<Room> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find rooms by price range with pagination.
     * Used for price filtering with pagination.
     * 
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of rooms within the price range
     */
    Page<Room> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find rooms by currency.
     * Used for filtering rooms by pricing currency.
     * 
     * @param currency the currency code (e.g., "USD", "EUR")
     * @return list of rooms with the specified currency
     */
    List<Room> findByCurrency(String currency);

    /**
     * Find rooms by equipment containing pattern.
     * Used for filtering rooms by available equipment.
     * 
     * @param equipmentPattern pattern for equipment (case-insensitive)
     * @return list of rooms with matching equipment
     */
    @Query("SELECT r FROM Room r WHERE LOWER(r.equipment) LIKE LOWER(CONCAT('%', :equipmentPattern, '%'))")
    List<Room> findByEquipmentContainingIgnoreCase(@Param("equipmentPattern") String equipmentPattern);

    /**
     * Find rooms by description containing pattern.
     * Used for searching rooms by description content.
     * 
     * @param descriptionPattern pattern for description (case-insensitive)
     * @return list of rooms with matching descriptions
     */
    @Query("SELECT r FROM Room r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :descriptionPattern, '%'))")
    List<Room> findByDescriptionContainingIgnoreCase(@Param("descriptionPattern") String descriptionPattern);

    /**
     * Find available rooms for a specific date range.
     * Used for booking availability checks.
     * 
     * @param checkIn check-in date
     * @param checkOut check-out date
     * @return list of available rooms for the date range
     */
    @Query("SELECT r FROM Room r WHERE r.status != 'OUT_OF_SERVICE' AND r.id NOT IN " +
           "(SELECT res.room.id FROM Reservation res WHERE " +
           "res.status = 'CONFIRMED' AND " +
           "(res.checkIn < :checkOut AND res.checkOut > :checkIn))")
    List<Room> findAvailableRoomsForDateRange(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * Find available rooms for a specific date range with additional filters.
     * Used for booking availability checks with room type and capacity filters.
     * 
     * @param checkIn check-in date
     * @param checkOut check-out date
     * @param roomType the room type (optional)
     * @param minCapacity minimum capacity (optional)
     * @return list of available rooms for the date range
     */
    @Query("SELECT r FROM Room r WHERE r.status != 'OUT_OF_SERVICE' AND r.id NOT IN " +
           "(SELECT res.room.id FROM Reservation res WHERE " +
           "res.status = 'CONFIRMED' AND " +
           "(res.checkIn < :checkOut AND res.checkOut > :checkIn)) AND " +
           "(:roomType IS NULL OR r.roomType = :roomType) AND " +
           "(:minCapacity IS NULL OR r.capacity >= :minCapacity)")
    List<Room> findAvailableRooms(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("roomType") RoomType roomType,
            @Param("minCapacity") Integer minCapacity
    );

    /**
     * Find available rooms for a specific date range with pagination.
     * Used for booking availability checks with pagination.
     * 
     * @param checkIn check-in date
     * @param checkOut check-out date
     * @param pageable pagination information
     * @return page of available rooms for the date range
     */
    @Query("SELECT r FROM Room r WHERE r.status != 'OUT_OF_SERVICE' AND r.id NOT IN " +
           "(SELECT res.room.id FROM Reservation res WHERE " +
           "res.status = 'CONFIRMED' AND " +
           "(res.checkIn < :checkOut AND res.checkOut > :checkIn))")
    Page<Room> findAvailableRoomsForDateRange(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            Pageable pageable
    );

    /**
     * Find available rooms for a specific date range with additional filters and pagination.
     * Used for booking availability checks with room type and capacity filters.
     * 
     * @param checkIn check-in date
     * @param checkOut check-out date
     * @param roomType the room type (optional)
     * @param minCapacity minimum capacity (optional)
     * @param pageable pagination information
     * @return page of available rooms for the date range
     */
    @Query("SELECT r FROM Room r WHERE r.status != 'OUT_OF_SERVICE' AND " +
           "(:roomType IS NULL OR r.roomType = :roomType) AND " +
           "(:minCapacity IS NULL OR r.capacity >= :minCapacity) AND " +
           "r.id NOT IN (SELECT res.room.id FROM Reservation res WHERE " +
           "res.status = 'CONFIRMED' AND " +
           "(res.checkIn < :checkOut AND res.checkOut > :checkIn))")
    Page<Room> findAvailableRoomsForDateRangeWithFilters(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("roomType") RoomType roomType,
            @Param("minCapacity") Integer minCapacity,
            Pageable pageable
    );

    /**
     * Find rooms by multiple criteria.
     * Used for advanced room search functionality.
     * 
     * @param roomType the room type (optional)
     * @param minCapacity minimum capacity (optional)
     * @param maxPrice maximum price (optional)
     * @param status the room status (optional)
     * @param pageable pagination information
     * @return page of rooms matching the criteria
     */
    @Query("SELECT r FROM Room r WHERE " +
           "(:roomType IS NULL OR r.roomType = :roomType) AND " +
           "(:minCapacity IS NULL OR r.capacity >= :minCapacity) AND " +
           "(:maxPrice IS NULL OR r.price <= :maxPrice) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<Room> findByCriteria(
            @Param("roomType") RoomType roomType,
            @Param("minCapacity") Integer minCapacity,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") RoomStatus status,
            Pageable pageable
    );

    /**
     * Count rooms by status.
     * Used for dashboard statistics.
     * 
     * @param status the room status
     * @return count of rooms with the specified status
     */
    long countByStatus(RoomStatus status);

    /**
     * Count rooms by room type.
     * Used for room type statistics.
     * 
     * @param roomType the room type
     * @return count of rooms with the specified type
     */
    long countByRoomType(RoomType roomType);

    /**
     * Find rooms created within a date range.
     * Used for analytics and reporting.
     * 
     * @param startDate start of creation date range
     * @param endDate end of creation date range
     * @param pageable pagination information
     * @return page of rooms created within the date range
     */
    Page<Room> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find recently created rooms.
     * Used for new room notifications and analytics.
     * 
     * @param sinceDate rooms created after this date
     * @param pageable pagination information
     * @return page of recently created rooms
     */
    Page<Room> findByCreatedAtAfter(LocalDateTime sinceDate, Pageable pageable);

    /**
     * Find rooms by room type and status.
     * Used for filtering available rooms by type.
     * 
     * @param roomType the room type
     * @param status the room status
     * @return list of rooms with the specified type and status
     */
    List<Room> findByRoomTypeAndStatus(RoomType roomType, RoomStatus status);

    /**
     * Find rooms by room type and status with pagination.
     * Used for filtering available rooms by type with pagination.
     * 
     * @param roomType the room type
     * @param status the room status
     * @param pageable pagination information
     * @return page of rooms with the specified type and status
     */
    Page<Room> findByRoomTypeAndStatus(RoomType roomType, RoomStatus status, Pageable pageable);

    /**
     * Find the cheapest available room.
     * Used for finding the best price option.
     * 
     * @return Optional containing the cheapest available room
     */
    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' ORDER BY r.price ASC, id ASC LIMIT 1")
    Optional<Room> findCheapestAvailableRoom();

    /**
     * Find the most expensive room.
     * Used for finding the premium option.
     * 
     * @return Optional containing the most expensive room
     */
    @Query("SELECT r FROM Room r ORDER BY r.price DESC, id ASC LIMIT 1")
    Optional<Room> findMostExpensiveRoom();

    @Query("SELECT r FROM Room r WHERE r.status <> com.MyBooking.room.domain.RoomStatus.OUT_OF_SERVICE " +
           "AND r.capacity >= :numberOfGuests " +
           "AND NOT EXISTS (" +
           "  SELECT res FROM Reservation res WHERE res.room = r " +
           "  AND res.status = com.MyBooking.reservation.domain.ReservationStatus.CONFIRMED " +
           "  AND res.checkIn < :checkOut AND res.checkOut > :checkIn" +
           ")")
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut,
                                  @Param("numberOfGuests") Integer numberOfGuests);
}
