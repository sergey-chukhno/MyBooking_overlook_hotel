package com.MyBooking.employee.repository;

import com.MyBooking.employee.domain.Shift;
import com.MyBooking.auth.domain.User;
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
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    
    // ==================== BASIC QUERIES ====================

    Optional<Shift> findByPublicId(UUID publicId);

    Optional<Shift> findByPublicIdAndEmployeeId(UUID publicId, Long employeeId);

    boolean existsByPublicId(UUID publicId);
    
    Optional<Shift> findByEmployee(User employee);
    List<Shift> findByEmployeeId(Long employeeId);
    boolean existsByEmployee(User employee);
    boolean existsByEmployeeId(Long employeeId);
    
    // ==================== TIME-BASED QUERIES ====================
    
    List<Shift> findByStartAtAfter(LocalDateTime startTime);
    List<Shift> findByStartAtBefore(LocalDateTime startTime);
    List<Shift> findByStartAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<Shift> findByEndAtAfter(LocalDateTime endTime);
    List<Shift> findByEndAtBefore(LocalDateTime endTime);
    List<Shift> findByEndAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // ==================== OVERLAP DETECTION ====================
    
    @Query("SELECT s FROM Shift s WHERE s.startAt < :endTime AND s.endAt > :startTime")
    List<Shift> findOverlappingShifts(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT s FROM Shift s WHERE s.startAt < :endTime AND s.endAt > :startTime AND s.employee = :employee")
    List<Shift> findOverlappingShiftsForEmployee(@Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime, 
                                                @Param("employee") User employee);
    
    @Query("SELECT s FROM Shift s WHERE s.startAt < :endTime AND s.endAt > :startTime AND s.employee.id = :employeeId")
    List<Shift> findOverlappingShiftsForEmployeeId(@Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime, 
                                                  @Param("employeeId") Long employeeId);

    @Query("SELECT s FROM Shift s WHERE s.startAt < :endTime AND s.endAt > :startTime " +
           "AND s.employee.id = :employeeId AND (:excludeShiftId IS NULL OR s.id != :excludeShiftId)")
    List<Shift> findOverlappingShiftsForEmployeeIdExcludingShiftId(@Param("startTime") LocalDateTime startTime, 
                                                                  @Param("endTime") LocalDateTime endTime, 
                                                                  @Param("employeeId") Long employeeId,
                                                                  @Param("excludeShiftId") Long excludeShiftId);
    
    // ==================== EMPLOYEE AND TIME COMBINED QUERIES ====================
    
    List<Shift> findByEmployeeAndStartAtAfter(User employee, LocalDateTime startTime);
    List<Shift> findByEmployeeAndStartAtBefore(User employee, LocalDateTime startTime);
    List<Shift> findByEmployeeAndStartAtBetween(User employee, LocalDateTime startTime, LocalDateTime endTime);
    List<Shift> findByEmployeeAndEndAtAfter(User employee, LocalDateTime endTime);
    List<Shift> findByEmployeeAndEndAtBefore(User employee, LocalDateTime endTime);
    List<Shift> findByEmployeeAndEndAtBetween(User employee, LocalDateTime startTime, LocalDateTime endTime);
    
    List<Shift> findByEmployeeIdAndStartAtAfter(Long employeeId, LocalDateTime startTime);
    List<Shift> findByEmployeeIdAndStartAtBefore(Long employeeId, LocalDateTime startTime);
    List<Shift> findByEmployeeIdAndStartAtBetween(Long employeeId, LocalDateTime startTime, LocalDateTime endTime);
    List<Shift> findByEmployeeIdAndEndAtAfter(Long employeeId, LocalDateTime endTime);
    List<Shift> findByEmployeeIdAndEndAtBefore(Long employeeId, LocalDateTime endTime);
    List<Shift> findByEmployeeIdAndEndAtBetween(Long employeeId, LocalDateTime startTime, LocalDateTime endTime);
    
    // ==================== PAGINATION SUPPORT ====================
    
    Page<Shift> findByEmployee(User employee, Pageable pageable);
    Page<Shift> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<Shift> findByStartAtAfter(LocalDateTime startTime, Pageable pageable);
    Page<Shift> findByStartAtBefore(LocalDateTime startTime, Pageable pageable);
    Page<Shift> findByStartAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Shift> findByEndAtAfter(LocalDateTime endTime, Pageable pageable);
    Page<Shift> findByEndAtBefore(LocalDateTime endTime, Pageable pageable);
    Page<Shift> findByEndAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // ==================== BASIC STATISTICS ====================
    
    @Query("SELECT COUNT(s) FROM Shift s")
    long getTotalShiftCount();
    
    @Query("SELECT COUNT(s) FROM Shift s WHERE s.employee = :employee")
    long getShiftCountByEmployee(@Param("employee") User employee);
    
    @Query("SELECT COUNT(s) FROM Shift s WHERE s.employee.id = :employeeId")
    long getShiftCountByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT COUNT(s) FROM Shift s WHERE s.startAt >= :startTime AND s.endAt <= :endTime")
    long getShiftCountInDateRange(@Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime);
    
    // ==================== VALIDATION QUERIES ====================
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Shift s WHERE s.employee = :employee AND s.startAt < :endTime AND s.endAt > :startTime")
    boolean hasOverlappingShift(@Param("employee") User employee, 
                               @Param("startTime") LocalDateTime startTime, 
                               @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Shift s WHERE s.employee.id = :employeeId AND s.startAt < :endTime AND s.endAt > :startTime")
    boolean hasOverlappingShiftForEmployeeId(@Param("employeeId") Long employeeId, 
                                            @Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
    
    // ==================== RECENT SHIFTS ====================
    
    @Query("SELECT s FROM Shift s WHERE s.startAt >= :date ORDER BY s.startAt DESC")
    List<Shift> findRecentShifts(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Shift s WHERE s.employee = :employee AND s.startAt >= :date ORDER BY s.startAt DESC")
    List<Shift> findRecentShiftsByEmployee(@Param("employee") User employee, 
                                          @Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Shift s WHERE s.employee.id = :employeeId AND s.startAt >= :date ORDER BY s.startAt DESC")
    List<Shift> findRecentShiftsByEmployeeId(@Param("employeeId") Long employeeId, 
                                            @Param("date") LocalDateTime date);

    @Query("SELECT s FROM Shift s WHERE " +
           "(:employeeId IS NULL OR s.employee.id = :employeeId) AND " +
           "(:startAt IS NULL OR s.startAt >= :startAt) AND " +
           "(:endAt IS NULL OR s.endAt <= :endAt)")
    Page<Shift> searchShifts(@Param("employeeId") Long employeeId,
                             @Param("startAt") LocalDateTime startAt,
                             @Param("endAt") LocalDateTime endAt,
                             Pageable pageable);
}