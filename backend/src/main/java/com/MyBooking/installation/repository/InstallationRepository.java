package com.MyBooking.installation.repository;

import com.MyBooking.installation.domain.Installation;
import com.MyBooking.installation.domain.InstallationType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstallationRepository extends JpaRepository<Installation, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<Installation> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Installation i WHERE i.id = :id")
    Optional<Installation> findByIdForUpdate(@Param("id") Long id);

    // Find by name (exact match)
    Optional<Installation> findByName(String name);
    
    // Find by name (case-insensitive)
    Optional<Installation> findByNameIgnoreCase(String name);
    
    // Find by installation type
    List<Installation> findByInstallationType(InstallationType installationType);
    Page<Installation> findByInstallationType(InstallationType installationType, Pageable pageable);

    // ==================== CAPACITY QUERIES ====================

    // Find by exact capacity
    List<Installation> findByCapacity(Integer capacity);
    Page<Installation> findByCapacity(Integer capacity, Pageable pageable);

    // Find by capacity greater than or equal
    List<Installation> findByCapacityGreaterThanEqual(Integer minCapacity);
    Page<Installation> findByCapacityGreaterThanEqual(Integer minCapacity, Pageable pageable);

    // Find by capacity less than or equal
    List<Installation> findByCapacityLessThanEqual(Integer maxCapacity);
    Page<Installation> findByCapacityLessThanEqual(Integer maxCapacity, Pageable pageable);

    // Find by capacity range
    List<Installation> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);
    Page<Installation> findByCapacityBetween(Integer minCapacity, Integer maxCapacity, Pageable pageable);

    // ==================== PRICING QUERIES ====================

    // Find by exact hourly rate
    List<Installation> findByHourlyRate(BigDecimal hourlyRate);
    Page<Installation> findByHourlyRate(BigDecimal hourlyRate, Pageable pageable);

    // Find by hourly rate greater than or equal
    List<Installation> findByHourlyRateGreaterThanEqual(BigDecimal minRate);
    Page<Installation> findByHourlyRateGreaterThanEqual(BigDecimal minRate, Pageable pageable);

    // Find by hourly rate less than or equal
    List<Installation> findByHourlyRateLessThanEqual(BigDecimal maxRate);
    Page<Installation> findByHourlyRateLessThanEqual(BigDecimal maxRate, Pageable pageable);

    // Find by hourly rate range
    List<Installation> findByHourlyRateBetween(BigDecimal minRate, BigDecimal maxRate);
    Page<Installation> findByHourlyRateBetween(BigDecimal minRate, BigDecimal maxRate, Pageable pageable);

    // Find by currency
    List<Installation> findByCurrency(String currency);
    Page<Installation> findByCurrency(String currency, Pageable pageable);

    // ==================== SEARCH QUERIES ====================

    // Find by name containing (case-insensitive)
    List<Installation> findByNameContainingIgnoreCase(String name);
    Page<Installation> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find by description containing (case-insensitive)
    List<Installation> findByDescriptionContainingIgnoreCase(String description);
    Page<Installation> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    // Find by equipment containing (case-insensitive)
    List<Installation> findByEquipmentContainingIgnoreCase(String equipment);
    Page<Installation> findByEquipmentContainingIgnoreCase(String equipment, Pageable pageable);

    // ==================== COMBINED QUERIES ====================

    // Find by type and capacity
    List<Installation> findByInstallationTypeAndCapacity(InstallationType type, Integer capacity);
    Page<Installation> findByInstallationTypeAndCapacity(InstallationType type, Integer capacity, Pageable pageable);

    // Find by type and capacity range
    List<Installation> findByInstallationTypeAndCapacityBetween(InstallationType type, Integer minCapacity, Integer maxCapacity);
    Page<Installation> findByInstallationTypeAndCapacityBetween(InstallationType type, Integer minCapacity, Integer maxCapacity, Pageable pageable);

    // Find by type and hourly rate range
    List<Installation> findByInstallationTypeAndHourlyRateBetween(InstallationType type, BigDecimal minRate, BigDecimal maxRate);
    Page<Installation> findByInstallationTypeAndHourlyRateBetween(InstallationType type, BigDecimal minRate, BigDecimal maxRate, Pageable pageable);

    // Find by capacity and hourly rate range
    List<Installation> findByCapacityBetweenAndHourlyRateBetween(Integer minCapacity, Integer maxCapacity, BigDecimal minRate, BigDecimal maxRate);
    Page<Installation> findByCapacityBetweenAndHourlyRateBetween(Integer minCapacity, Integer maxCapacity, BigDecimal minRate, BigDecimal maxRate, Pageable pageable);

    // ==================== DATE-BASED QUERIES ====================

    // Find by creation date range
    List<Installation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Installation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by creation date after
    List<Installation> findByCreatedAtAfter(LocalDateTime date);
    Page<Installation> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    // Find by creation date before
    List<Installation> findByCreatedAtBefore(LocalDateTime date);
    Page<Installation> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);

    // ==================== CUSTOM BUSINESS QUERIES ====================

    // Find installations suitable for a specific number of participants
    @Query("SELECT i FROM Installation i WHERE i.capacity >= :participants ORDER BY i.capacity ASC")
    List<Installation> findSuitableForParticipants(@Param("participants") Integer participants);
    @Query("SELECT i FROM Installation i WHERE i.capacity >= :participants ORDER BY i.capacity ASC")
    Page<Installation> findSuitableForParticipants(@Param("participants") Integer participants, Pageable pageable);

    // Find installations by type ordered by capacity descending
    @Query("SELECT i FROM Installation i WHERE i.installationType = :type ORDER BY i.capacity DESC")
    List<Installation> findByTypeOrderByCapacityDesc(@Param("type") InstallationType type);
    @Query("SELECT i FROM Installation i WHERE i.installationType = :type ORDER BY i.capacity DESC")
    Page<Installation> findByTypeOrderByCapacityDesc(@Param("type") InstallationType type, Pageable pageable);

    // Find installations by type ordered by hourly rate ascending
    @Query("SELECT i FROM Installation i WHERE i.installationType = :type ORDER BY i.hourlyRate ASC")
    List<Installation> findByTypeOrderByHourlyRateAsc(@Param("type") InstallationType type);
    @Query("SELECT i FROM Installation i WHERE i.installationType = :type ORDER BY i.hourlyRate ASC")
    Page<Installation> findByTypeOrderByHourlyRateAsc(@Param("type") InstallationType type, Pageable pageable);

    // Find high-capacity installations
    @Query("SELECT i FROM Installation i WHERE i.capacity >= :minCapacity ORDER BY i.capacity DESC")
    List<Installation> findHighCapacityInstallations(@Param("minCapacity") Integer minCapacity);
    @Query("SELECT i FROM Installation i WHERE i.capacity >= :minCapacity ORDER BY i.capacity DESC")
    Page<Installation> findHighCapacityInstallations(@Param("minCapacity") Integer minCapacity, Pageable pageable);

    // Find premium installations (high hourly rate)
    @Query("SELECT i FROM Installation i WHERE i.hourlyRate >= :minRate ORDER BY i.hourlyRate DESC")
    List<Installation> findPremiumInstallations(@Param("minRate") BigDecimal minRate);
    @Query("SELECT i FROM Installation i WHERE i.hourlyRate >= :minRate ORDER BY i.hourlyRate DESC")
    Page<Installation> findPremiumInstallations(@Param("minRate") BigDecimal minRate, Pageable pageable);

    // ==================== COUNT QUERIES ====================

    // Count by installation type
    long countByInstallationType(InstallationType installationType);

    // Count by capacity
    long countByCapacity(Integer capacity);
    long countByCapacityGreaterThanEqual(Integer minCapacity);
    long countByCapacityLessThanEqual(Integer maxCapacity);
    long countByCapacityBetween(Integer minCapacity, Integer maxCapacity);

    // Count by hourly rate
    long countByHourlyRate(BigDecimal hourlyRate);
    long countByHourlyRateGreaterThanEqual(BigDecimal minRate);
    long countByHourlyRateLessThanEqual(BigDecimal maxRate);
    long countByHourlyRateBetween(BigDecimal minRate, BigDecimal maxRate);

    // Count by currency
    long countByCurrency(String currency);

    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBefore(LocalDateTime date);

    // ==================== EXISTENCE QUERIES ====================

    // Check if installation exists by name
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);

    // Check if any installation exists with capacity greater than
    boolean existsByCapacityGreaterThanEqual(Integer minCapacity);

    // Check if any installation exists with hourly rate greater than
    boolean existsByHourlyRateGreaterThanEqual(BigDecimal minRate);

    // ==================== AGGREGATION QUERIES ====================

    // Get total number of installations
    long count();

    // Get average capacity by installation type
    @Query("SELECT i.installationType, AVG(i.capacity) FROM Installation i GROUP BY i.installationType")
    List<Object[]> getAverageCapacityByType();

    // Get average hourly rate by installation type
    @Query("SELECT i.installationType, AVG(i.hourlyRate) FROM Installation i GROUP BY i.installationType")
    List<Object[]> getAverageHourlyRateByType();

    // Get capacity statistics by installation type
    @Query("SELECT i.installationType, MIN(i.capacity), MAX(i.capacity), AVG(i.capacity), COUNT(i) FROM Installation i GROUP BY i.installationType")
    List<Object[]> getCapacityStatisticsByType();

    // Get hourly rate statistics by installation type
    @Query("SELECT i.installationType, MIN(i.hourlyRate), MAX(i.hourlyRate), AVG(i.hourlyRate), COUNT(i) FROM Installation i GROUP BY i.installationType")
    List<Object[]> getHourlyRateStatisticsByType();

    // Get total capacity by installation type
    @Query("SELECT i.installationType, SUM(i.capacity) FROM Installation i GROUP BY i.installationType")
    List<Object[]> getTotalCapacityByType();

    // Get installations with no events (unused installations)
    @Query("SELECT i FROM Installation i WHERE i.events IS EMPTY")
    List<Installation> findUnusedInstallations();
    @Query("SELECT i FROM Installation i WHERE i.events IS EMPTY")
    Page<Installation> findUnusedInstallations(Pageable pageable);

    // Get installations with events (used installations)
    @Query("SELECT i FROM Installation i WHERE i.events IS NOT EMPTY")
    List<Installation> findUsedInstallations();
    @Query("SELECT i FROM Installation i WHERE i.events IS NOT EMPTY")
    Page<Installation> findUsedInstallations(Pageable pageable);

    @Query("SELECT COUNT(i) FROM Installation i WHERE i.events IS NOT EMPTY")
    long countUsedInstallations();

    // ==================== CUSTOM QUERIES FOR API RESPONSES ====================

    // Find all installations without loading events (to avoid circular reference)
    @Query("SELECT i FROM Installation i")
    List<Installation> findAllWithoutEvents();
    
    // Find all installations without loading events (paginated)
    @Query("SELECT i FROM Installation i")
    Page<Installation> findAllWithoutEvents(Pageable pageable);
    
    // Find installation by ID without loading events
    @Query("SELECT i FROM Installation i WHERE i.id = :id")
    Optional<Installation> findByIdWithoutEvents(@Param("id") Long id);

    // ==================== PROJECTION QUERIES FOR API RESPONSES ====================

    // Get installation data as projection to avoid circular reference
    @Query("SELECT new com.MyBooking.installation.dto.InstallationResponseDto(" +
           "i.id, i.name, i.description, i.installationType, i.capacity, " +
           "i.hourlyRate, i.currency, i.equipment, i.createdAt, i.updatedAt) " +
           "FROM Installation i")
    List<com.MyBooking.installation.dto.InstallationResponseDto> findAllAsProjection();
    
    // Get installation data as projection by ID
    @Query("SELECT new com.MyBooking.installation.dto.InstallationResponseDto(" +
           "i.id, i.name, i.description, i.installationType, i.capacity, " +
           "i.hourlyRate, i.currency, i.equipment, i.createdAt, i.updatedAt) " +
           "FROM Installation i WHERE i.id = :id")
    Optional<com.MyBooking.installation.dto.InstallationResponseDto> findByIdAsProjection(@Param("id") Long id);

    // ==================== AVAILABILITY QUERIES ====================

    @Query("SELECT i FROM Installation i WHERE i.capacity >= :minCapacity " +
           "AND (:installationType IS NULL OR i.installationType = :installationType) " +
           "AND NOT EXISTS (" +
           "  SELECT e FROM Event e WHERE e.installation = i " +
           "  AND e.startAt < :endTime AND e.endAt > :startTime" +
           ")")
    List<Installation> findAvailableInstallations(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("minCapacity") Integer minCapacity,
                                                  @Param("installationType") InstallationType installationType);
}