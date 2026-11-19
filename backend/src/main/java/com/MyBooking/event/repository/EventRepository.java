package com.MyBooking.event.repository;

import com.MyBooking.event.domain.Event;
import com.MyBooking.event.domain.EventType;
import com.MyBooking.installation.domain.Installation;
import com.MyBooking.installation.domain.InstallationType;
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
 * Repository interface for Event entity operations.
 * Provides data access methods for event management, filtering, and search functionality.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"installation"})
    Optional<Event> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    // ==================== CONCURRENCY LOCKING METHODS ====================

    /**
     * Find event by ID with pessimistic write lock to serialize concurrent bookings.
     * Prevents overbooking race conditions [INV-EVT-001].
     * 
     * @param id the event ID
     * @return Optional containing locked event
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    // ==================== BASIC FINDER METHODS ====================
    
    /**
     * Find event by name.
     * Used for event lookup and validation.
     * 
     * @param name the event name
     * @return Optional containing the event if found
     */
    Optional<Event> findByName(String name);
    
    /**
     * Check if an event exists with the given name.
     * Used for event name validation during creation.
     * 
     * @param name the event name
     * @return true if event exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find events by event type.
     * Used for filtering events by category.
     * 
     * @param eventType the event type
     * @return list of events with the specified type
     */
    List<Event> findByEventType(EventType eventType);
    
    /**
     * Find events by event type with pagination.
     * Used for event type filtering with pagination.
     * 
     * @param eventType the event type
     * @param pageable pagination information
     * @return page of events with the specified type
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"installation"})
    Page<Event> findByEventType(EventType eventType, Pageable pageable);
    
    /**
     * Find events by installation.
     * Used for filtering events by installation.
     * 
     * @param installation the installation
     * @return list of events for the specified installation
     */
    List<Event> findByInstallation(Installation installation);
    
    /**
     * Find events by installation ID.
     * Used for filtering events by installation ID.
     * 
     * @param installationId the installation ID
     * @return list of events for the specified installation
     */
    List<Event> findByInstallationId(Long installationId);
    
    /**
     * Find events by installation ID with pagination.
     * Used for installation filtering with pagination.
     * 
     * @param installationId the installation ID
     * @param pageable pagination information
     * @return page of events for the specified installation
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"installation"})
    Page<Event> findByInstallationId(Long installationId, Pageable pageable);

    // ==================== PRICE-BASED QUERIES ====================
    
    /**
     * Find events by price range.
     * Used for filtering events within a price range.
     * 
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of events within the price range
     */
    List<Event> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find events by price range with pagination.
     * Used for price range filtering with pagination.
     * 
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of events within the price range
     */
    Page<Event> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Find events with price greater than or equal to specified value.
     * Used for filtering events above a minimum price.
     * 
     * @param minPrice minimum price
     * @return list of events above the minimum price
     */
    List<Event> findByPriceGreaterThanEqual(BigDecimal minPrice);
    
    /**
     * Find events with price less than or equal to specified value.
     * Used for filtering events below a maximum price.
     * 
     * @param maxPrice maximum price
     * @return list of events below the maximum price
     */
    List<Event> findByPriceLessThanEqual(BigDecimal maxPrice);

    // ==================== CAPACITY-BASED QUERIES ====================
    
    /**
     * Find events by capacity range.
     * Used for filtering events within a capacity range.
     * 
     * @param minCapacity minimum capacity
     * @param maxCapacity maximum capacity
     * @return list of events within the capacity range
     */
    List<Event> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);
    
    /**
     * Find events by capacity range with pagination.
     * Used for capacity range filtering with pagination.
     * 
     * @param minCapacity minimum capacity
     * @param maxCapacity maximum capacity
     * @param pageable pagination information
     * @return page of events within the capacity range
     */
    Page<Event> findByCapacityBetween(Integer minCapacity, Integer maxCapacity, Pageable pageable);
    
    /**
     * Find events with capacity greater than or equal to specified value.
     * Used for filtering events above a minimum capacity.
     * 
     * @param minCapacity minimum capacity
     * @return list of events above the minimum capacity
     */
    List<Event> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    /**
     * Find events with capacity less than or equal to specified value.
     * Used for filtering events below a maximum capacity.
     * 
     * @param maxCapacity maximum capacity
     * @return list of events below the maximum capacity
     */
    List<Event> findByCapacityLessThanEqual(Integer maxCapacity);

    // ==================== CURRENCY-BASED QUERIES ====================
    
    /**
     * Find events by currency.
     * Used for filtering events by currency.
     * 
     * @param currency the currency code
     * @return list of events with the specified currency
     */
    List<Event> findByCurrency(String currency);
    
    /**
     * Find events by currency with pagination.
     * Used for currency filtering with pagination.
     * 
     * @param currency the currency code
     * @param pageable pagination information
     * @return page of events with the specified currency
     */
    Page<Event> findByCurrency(String currency, Pageable pageable);

    // ==================== SEARCH AND FILTERING QUERIES ====================
    
    /**
     * Find events by name containing (case-insensitive).
     * Used for event name search functionality.
     * 
     * @param name the name to search for
     * @return list of events with names containing the search term
     */
    List<Event> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find events by name containing with pagination.
     * Used for event name search with pagination.
     * 
     * @param name the name to search for
     * @param pageable pagination information
     * @return page of events with names containing the search term
     */
    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find events by keyword search in name or description.
     * Used for comprehensive event search functionality.
     * 
     * @param keyword the keyword to search for
     * @return list of events matching the keyword
     */
    @Query("SELECT e FROM Event e WHERE e.name LIKE %:keyword% OR e.description LIKE %:keyword%")
    List<Event> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * Find events by keyword search with pagination.
     * Used for comprehensive event search with pagination.
     * 
     * @param keyword the keyword to search for
     * @param pageable pagination information
     * @return page of events matching the keyword
     */
    @Query("SELECT e FROM Event e WHERE e.name LIKE %:keyword% OR e.description LIKE %:keyword%")
    Page<Event> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ==================== TIME-BASED QUERIES ====================
    
    /**
     * Find events by start time range.
     * Used for filtering events within a time range.
     * 
     * @param startTime start time
     * @param endTime end time
     * @return list of events starting within the time range
     */
    @Query("SELECT e FROM Event e WHERE e.startAt >= :startTime AND e.startAt <= :endTime")
    List<Event> findByStartTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find events by end time range.
     * Used for filtering events ending within a time range.
     * 
     * @param startTime start time
     * @param endTime end time
     * @return list of events ending within the time range
     */
    @Query("SELECT e FROM Event e WHERE e.endAt >= :startTime AND e.endAt <= :endTime")
    List<Event> findByEndTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * Check if events are available for the given time range.
     * Returns conflicting events if any exist.
     * 
     * @param startTime start time
     * @param endTime end time
     * @return list of conflicting events (empty if time slot is available)
     */
    @Query("SELECT e FROM Event e WHERE " +
           "(e.startAt <= :startTime AND e.endAt > :startTime) OR " +
           "(e.startAt < :endTime AND e.endAt >= :endTime) OR " +
           "(e.startAt >= :startTime AND e.endAt <= :endTime)")
    List<Event> checkEventAvailability(@Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * Check whether any event exists for a specific installation in the given half-open time range [startTime, endTime).
     */
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.installation.id = :installationId " +
           "AND e.startAt < :endTime AND e.endAt > :startTime")
    boolean existsOverlappingEventForInstallation(@Param("installationId") Long installationId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * Check whether any other event (excluding excludeEventId) exists for a specific installation
     * in the given half-open time range [startTime, endTime).
     */
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.installation.id = :installationId " +
           "AND e.id <> :excludeEventId " +
           "AND e.startAt < :endTime AND e.endAt > :startTime")
    boolean existsOverlappingEventForInstallationExcludingEvent(@Param("installationId") Long installationId,
                                                                @Param("startTime") LocalDateTime startTime,
                                                                @Param("endTime") LocalDateTime endTime,
                                                                @Param("excludeEventId") Long excludeEventId);
    
    /**
     * Find events created between specified dates.
     * Used for time-based event analytics.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of events created in the date range
     */
    @Query("SELECT e FROM Event e WHERE e.createdAt >= :startDate AND e.createdAt <= :endDate")
    List<Event> findEventsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find events updated between specified dates.
     * Used for time-based event analytics.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of events updated in the date range
     */
    @Query("SELECT e FROM Event e WHERE e.updatedAt >= :startDate AND e.updatedAt <= :endDate")
    List<Event> findEventsUpdatedBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    // ==================== COMBINED CRITERIA QUERIES ====================
    
    /**
     * Find events by event type and price range.
     * Used for filtering events by type and price.
     * 
     * @param eventType the event type
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of events matching the criteria
     */
    @Query("SELECT e FROM Event e WHERE e.eventType = :eventType AND e.price BETWEEN :minPrice AND :maxPrice")
    List<Event> findByEventTypeAndPriceRange(@Param("eventType") EventType eventType, 
                                           @Param("minPrice") BigDecimal minPrice, 
                                           @Param("maxPrice") BigDecimal maxPrice);
    
    /**
     * Find events by installation and minimum capacity.
     * Used for filtering events by installation and capacity.
     * 
     * @param installationId the installation ID
     * @param minCapacity minimum capacity
     * @return list of events matching the criteria
     */
    @Query("SELECT e FROM Event e WHERE e.installation.id = :installationId AND e.capacity >= :minCapacity")
    List<Event> findByInstallationAndMinCapacity(@Param("installationId") Long installationId, 
                                                @Param("minCapacity") Integer minCapacity);
    
    /**
     * Find events that can accommodate a specific number of participants.
     * Used for finding events that can accommodate the requested participants.
     * 
     * @param participants the number of participants
     * @return list of events that can accommodate the participants
     */
    @Query("SELECT e FROM Event e WHERE e.capacity >= :participants")
    List<Event> findEventsForParticipants(@Param("participants") Integer participants);

    // ==================== STATISTICS AND ANALYTICS ====================
    
    /**
     * Count events by event type.
     * Used for event type statistics.
     * 
     * @param eventType the event type
     * @return count of events with the specified type
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.eventType = :eventType")
    Long countByEventType(@Param("eventType") EventType eventType);
    
    /**
     * Count events by installation.
     * Used for installation usage statistics.
     * 
     * @param installationId the installation ID
     * @return count of events for the specified installation
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.installation.id = :installationId")
    Long countByInstallationId(@Param("installationId") Long installationId);
    
    /**
     * Count events by currency.
     * Used for currency usage statistics.
     * 
     * @param currency the currency code
     * @return count of events with the specified currency
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.currency = :currency")
    Long countByCurrency(@Param("currency") String currency);
    
    /**
     * Get average price by event type.
     * Used for price analytics by event type.
     * 
     * @param eventType the event type
     * @return average price for the event type
     */
    @Query("SELECT AVG(e.price) FROM Event e WHERE e.eventType = :eventType")
    Optional<BigDecimal> getAveragePriceByEventType(@Param("eventType") EventType eventType);
    
    /**
     * Get average price by currency.
     * Used for price analytics by currency.
     * 
     * @param currency the currency code
     * @return average price for the currency
     */
    @Query("SELECT AVG(e.price) FROM Event e WHERE e.currency = :currency")
    Optional<BigDecimal> getAveragePriceByCurrency(@Param("currency") String currency);
    
    /**
     * Get minimum price by event type.
     * Used for price analytics by event type.
     * 
     * @param eventType the event type
     * @return minimum price for the event type
     */
    @Query("SELECT MIN(e.price) FROM Event e WHERE e.eventType = :eventType")
    Optional<BigDecimal> getMinPriceByEventType(@Param("eventType") EventType eventType);
    
    /**
     * Get maximum price by event type.
     * Used for price analytics by event type.
     * 
     * @param eventType the event type
     * @return maximum price for the event type
     */
    @Query("SELECT MAX(e.price) FROM Event e WHERE e.eventType = :eventType")
    Optional<BigDecimal> getMaxPriceByEventType(@Param("eventType") EventType eventType);
    
    /**
     * Find the cheapest events.
     * Used for finding the most affordable events.
     * 
     * @return list of events with the lowest price
     */
    @Query("SELECT e FROM Event e WHERE e.price = (SELECT MIN(e2.price) FROM Event e2)")
    List<Event> findCheapestEvents();
    
    /**
     * Find the most expensive events.
     * Used for finding the highest-priced events.
     * 
     * @return list of events with the highest price
     */
    @Query("SELECT e FROM Event e WHERE e.price = (SELECT MAX(e2.price) FROM Event e2)")
    List<Event> findMostExpensiveEvents();

    // ==================== EXISTENCE CHECKS ====================
    
    /**
     * Check if events exist by event type.
     * Used for validation and business logic.
     * 
     * @param eventType the event type
     * @return true if events exist with the specified type
     */
    boolean existsByEventType(EventType eventType);
    
    /**
     * Check if events exist by installation.
     * Used for validation and business logic.
     * 
     * @param installationId the installation ID
     * @return true if events exist for the specified installation
     */
    boolean existsByInstallationId(Long installationId);
    
    /**
     * Check if events exist by currency.
     * Used for validation and business logic.
     * 
     * @param currency the currency code
     * @return true if events exist with the specified currency
     */
    boolean existsByCurrency(String currency);

    // ==================== ADVANCED SEARCH WITH MULTIPLE CRITERIA ====================
    
    /**
     * Find events by multiple criteria.
     * Used for advanced event search functionality.
     * 
     * @param name event name (optional)
     * @param eventType event type (optional)
     * @param installationId installation ID (optional)
     * @param minPrice minimum price (optional)
     * @param maxPrice maximum price (optional)
     * @param minCapacity minimum capacity (optional)
     * @param currency currency code (optional)
     * @return list of events matching the criteria
     */
    @EntityGraph(attributePaths = {"installation"})
    @Query("SELECT e FROM Event e WHERE " +
           "(:name IS NULL OR e.name LIKE %:name%) AND " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:installationId IS NULL OR e.installation.id = :installationId) AND " +
           "(:minPrice IS NULL OR e.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR e.price <= :maxPrice) AND " +
           "(:minCapacity IS NULL OR e.capacity >= :minCapacity) AND " +
           "(:currency IS NULL OR e.currency = :currency)")
    List<Event> findByCriteria(@Param("name") String name,
                              @Param("eventType") EventType eventType,
                              @Param("installationId") Long installationId,
                              @Param("minPrice") BigDecimal minPrice,
                              @Param("maxPrice") BigDecimal maxPrice,
                              @Param("minCapacity") Integer minCapacity,
                              @Param("currency") String currency);
    
    /**
     * Find events by multiple criteria with pagination.
     * Used for advanced event search with pagination.
     * 
     * @param name event name (optional)
     * @param eventType event type (optional)
     * @param installationId installation ID (optional)
     * @param minPrice minimum price (optional)
     * @param maxPrice maximum price (optional)
     * @param minCapacity minimum capacity (optional)
     * @param currency currency code (optional)
     * @param pageable pagination information
     * @return page of events matching the criteria
     */
    @EntityGraph(attributePaths = {"installation"})
    @Query("SELECT e FROM Event e WHERE " +
           "(:name IS NULL OR e.name LIKE %:name%) AND " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:installationId IS NULL OR e.installation.id = :installationId) AND " +
           "(:minPrice IS NULL OR e.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR e.price <= :maxPrice) AND " +
           "(:minCapacity IS NULL OR e.capacity >= :minCapacity) AND " +
           "(:currency IS NULL OR e.currency = :currency)")
    Page<Event> findByCriteria(@Param("name") String name,
                              @Param("eventType") EventType eventType,
                              @Param("installationId") Long installationId,
                              @Param("minPrice") BigDecimal minPrice,
                              @Param("maxPrice") BigDecimal maxPrice,
                              @Param("minCapacity") Integer minCapacity,
                              @Param("currency") String currency,
                              Pageable pageable);

    // ==================== P3-T06 PUSH-DOWN QUERIES ====================

    List<Event> findByStartAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Event> findByStartAtGreaterThanEqual(LocalDateTime now);

    @Override
    @EntityGraph(attributePaths = {"installation"})
    Page<Event> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"installation"})
    Page<Event> findByStartAtGreaterThanEqual(LocalDateTime now, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.price), 0) FROM Event e WHERE e.currency = :currency")
    BigDecimal getRevenueByCurrency(@Param("currency") String currency);

    @Query("SELECT e FROM Event e WHERE e.installation.installationType = :installationType")
    List<Event> findByInstallationType(@Param("installationType") InstallationType installationType);

    @EntityGraph(attributePaths = {"installation"})
    @Query("SELECT e FROM Event e WHERE e.installation.installationType = :installationType")
    Page<Event> findByInstallationType(@Param("installationType") InstallationType installationType, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.startAt >= :now")
    long countUpcomingEvents(@Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(e.price), 0) FROM Event e")
    BigDecimal getTotalEventRevenue();
}