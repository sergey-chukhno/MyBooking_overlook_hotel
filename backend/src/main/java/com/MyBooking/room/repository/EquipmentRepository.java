package com.MyBooking.room.repository;

import com.MyBooking.room.domain.Equipment;
import com.MyBooking.room.domain.EquipmentType;
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
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<Equipment> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    // Find by name (exact match)
    Optional<Equipment> findByName(String name);
    
    // Find by name (case-insensitive)
    Optional<Equipment> findByNameIgnoreCase(String name);
    
    // Find by equipment type
    List<Equipment> findByEquipmentType(EquipmentType equipmentType);
    Page<Equipment> findByEquipmentType(EquipmentType equipmentType, Pageable pageable);

    // ==================== AVAILABILITY QUERIES ====================

    // Find available equipment
    List<Equipment> findByIsActiveTrue();
    Page<Equipment> findByIsActiveTrue(Pageable pageable);
    
    // Find available equipment by type
    List<Equipment> findByEquipmentTypeAndIsActiveTrue(EquipmentType equipmentType);
    Page<Equipment> findByEquipmentTypeAndIsActiveTrue(EquipmentType equipmentType, Pageable pageable);
    
    // Find equipment with available quantity
    List<Equipment> findByAvailableQuantityGreaterThan(Integer minQuantity);
    Page<Equipment> findByAvailableQuantityGreaterThan(Integer minQuantity, Pageable pageable);
    
    // Find equipment by quantity range
    List<Equipment> findByQuantityBetween(Integer minQuantity, Integer maxQuantity);
    Page<Equipment> findByQuantityBetween(Integer minQuantity, Integer maxQuantity, Pageable pageable);
    
    // Find equipment by available quantity range
    List<Equipment> findByAvailableQuantityBetween(Integer minAvailable, Integer maxAvailable);
    Page<Equipment> findByAvailableQuantityBetween(Integer minAvailable, Integer maxAvailable, Pageable pageable);

    // ==================== SEARCH QUERIES ====================

    // Find by name containing (case-insensitive)
    List<Equipment> findByNameContainingIgnoreCase(String name);
    Page<Equipment> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Find by description containing (case-insensitive)
    List<Equipment> findByDescriptionContainingIgnoreCase(String description);
    Page<Equipment> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
    
    // Find by brand
    List<Equipment> findByBrand(String brand);
    Page<Equipment> findByBrand(String brand, Pageable pageable);
    
    // Find by brand containing (case-insensitive)
    List<Equipment> findByBrandContainingIgnoreCase(String brand);
    Page<Equipment> findByBrandContainingIgnoreCase(String brand, Pageable pageable);
    
    // Find by model
    List<Equipment> findByModel(String model);
    Page<Equipment> findByModel(String model, Pageable pageable);
    
    // Find by model containing (case-insensitive)
    List<Equipment> findByModelContainingIgnoreCase(String model);
    Page<Equipment> findByModelContainingIgnoreCase(String model, Pageable pageable);

    // ==================== COMBINED QUERIES ====================

    // Find by type and availability
    List<Equipment> findByEquipmentTypeAndIsActiveTrueAndAvailableQuantityGreaterThan(EquipmentType equipmentType, Integer minQuantity);
    Page<Equipment> findByEquipmentTypeAndIsActiveTrueAndAvailableQuantityGreaterThan(EquipmentType equipmentType, Integer minQuantity, Pageable pageable);
    
    // Find by type and brand
    List<Equipment> findByEquipmentTypeAndBrand(EquipmentType equipmentType, String brand);
    Page<Equipment> findByEquipmentTypeAndBrand(EquipmentType equipmentType, String brand, Pageable pageable);
    
    // Find by type and quantity range
    List<Equipment> findByEquipmentTypeAndQuantityBetween(EquipmentType equipmentType, Integer minQuantity, Integer maxQuantity);
    Page<Equipment> findByEquipmentTypeAndQuantityBetween(EquipmentType equipmentType, Integer minQuantity, Integer maxQuantity, Pageable pageable);

    // ==================== DATE-BASED QUERIES ====================

    // Find by creation date range
    List<Equipment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Equipment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by creation date after
    List<Equipment> findByCreatedAtAfter(LocalDateTime date);
    Page<Equipment> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);
    
    // Find by creation date before
    List<Equipment> findByCreatedAtBefore(LocalDateTime date);
    Page<Equipment> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);

    // ==================== CUSTOM BUSINESS QUERIES ====================

    // Find low stock equipment
    @Query("SELECT e FROM Equipment e WHERE e.availableQuantity <= :threshold AND e.isActive = true")
    List<Equipment> findLowStockEquipment(@Param("threshold") Integer threshold);
    @Query("SELECT e FROM Equipment e WHERE e.availableQuantity <= :threshold AND e.isActive = true")
    Page<Equipment> findLowStockEquipment(@Param("threshold") Integer threshold, Pageable pageable);
    
    // Find out of stock equipment
    @Query("SELECT e FROM Equipment e WHERE e.availableQuantity = 0 AND e.isActive = true")
    List<Equipment> findOutOfStockEquipment();
    @Query("SELECT e FROM Equipment e WHERE e.availableQuantity = 0 AND e.isActive = true")
    Page<Equipment> findOutOfStockEquipment(Pageable pageable);
    
    // Find equipment by type ordered by availability
    @Query("SELECT e FROM Equipment e WHERE e.equipmentType = :type AND e.isActive = true ORDER BY e.availableQuantity DESC")
    List<Equipment> findByTypeOrderByAvailabilityDesc(@Param("type") EquipmentType equipmentType);
    @Query("SELECT e FROM Equipment e WHERE e.equipmentType = :type AND e.isActive = true ORDER BY e.availableQuantity DESC")
    Page<Equipment> findByTypeOrderByAvailabilityDesc(@Param("type") EquipmentType equipmentType, Pageable pageable);

    // ==================== COUNT QUERIES ====================

    // Count by equipment type
    long countByEquipmentType(EquipmentType equipmentType);
    
    // Count by availability status
    long countByIsActiveTrue();
    long countByIsActiveFalse();
    
    // Count by quantity range
    long countByQuantityBetween(Integer minQuantity, Integer maxQuantity);
    
    // Count by available quantity range
    long countByAvailableQuantityBetween(Integer minAvailable, Integer maxAvailable);
    
    // Count by brand
    long countByBrand(String brand);
    
    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ==================== EXISTENCE QUERIES ====================

    // Check existence by name
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    
    // Check existence by type
    boolean existsByEquipmentType(EquipmentType equipmentType);
    
    // Check existence by brand
    boolean existsByBrand(String brand);
    
    // Check existence by availability
    boolean existsByIsActiveTrue();
    boolean existsByAvailableQuantityGreaterThan(Integer minQuantity);

    // ==================== AGGREGATION QUERIES ====================

    // Get total number of equipment
    long count();

    // Get average quantity by equipment type
    @Query("SELECT e.equipmentType, AVG(e.quantity) FROM Equipment e GROUP BY e.equipmentType")
    List<Object[]> getAverageQuantityByType();

    // Get quantity statistics by equipment type
    @Query("SELECT e.equipmentType, MIN(e.quantity), MAX(e.quantity), AVG(e.quantity), COUNT(e) FROM Equipment e GROUP BY e.equipmentType")
    List<Object[]> getQuantityStatisticsByType();

    // Get availability statistics by equipment type
    @Query("SELECT e.equipmentType, MIN(e.availableQuantity), MAX(e.availableQuantity), AVG(e.availableQuantity), COUNT(e) FROM Equipment e WHERE e.isActive = true GROUP BY e.equipmentType")
    List<Object[]> getAvailabilityStatisticsByType();

    // Get total quantity by equipment type
    @Query("SELECT e.equipmentType, SUM(e.quantity) FROM Equipment e GROUP BY e.equipmentType")
    List<Object[]> getTotalQuantityByType();

    // Get total available quantity by equipment type
    @Query("SELECT e.equipmentType, SUM(e.availableQuantity) FROM Equipment e WHERE e.isActive = true GROUP BY e.equipmentType")
    List<Object[]> getTotalAvailableQuantityByType();
}
