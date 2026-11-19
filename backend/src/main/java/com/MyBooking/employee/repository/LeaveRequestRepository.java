package com.MyBooking.employee.repository;

import com.MyBooking.auth.domain.User;
import com.MyBooking.employee.domain.LeaveRequest;
import com.MyBooking.employee.domain.LeaveRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    // ==================== BASIC QUERIES ====================

    Optional<LeaveRequest> findByPublicId(UUID publicId);

    Optional<LeaveRequest> findByPublicIdAndEmployeeId(UUID publicId, Long employeeId);

    boolean existsByPublicId(UUID publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.id = :id")
    Optional<LeaveRequest> findByIdForUpdate(@Param("id") Long id);

    boolean existsByEmployeeAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
        User employee,
        LeaveRequestStatus status,
        LocalDate dateForFrom,
        LocalDate dateForTo
    );
    
    /**
     * Find all leave requests for a specific employee
     */
    List<LeaveRequest> findByEmployee(User employee);
    
    /**
     * Find all leave requests for a specific employee by ID
     */
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    
    /**
     * Find all leave requests with a specific status
     */
    List<LeaveRequest> findByStatus(LeaveRequestStatus status);
    
    /**
     * Find all leave requests for a specific employee with a specific status
     */
    List<LeaveRequest> findByEmployeeAndStatus(User employee, LeaveRequestStatus status);
    
    /**
     * Find all leave requests for a specific employee by ID with a specific status
     */
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequestStatus status);
    
    // ==================== DATE RANGE QUERIES ====================
    
    /**
     * Find leave requests that start on or after a specific date
     */
    List<LeaveRequest> findByFromDateAfter(LocalDate date);
    
    /**
     * Find leave requests that end on or before a specific date
     */
    List<LeaveRequest> findByToDateBefore(LocalDate date);
    
    /**
     * Find leave requests that start between two dates (inclusive)
     */
    List<LeaveRequest> findByFromDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find leave requests that end between two dates (inclusive)
     */
    List<LeaveRequest> findByToDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find leave requests that overlap with a date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    List<LeaveRequest> findOverlappingLeaveRequests(@Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Find leave requests for a specific employee that overlap with a date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee = :employee AND lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    List<LeaveRequest> findOverlappingLeaveRequestsForEmployee(@Param("employee") User employee,
                                                              @Param("startDate") LocalDate startDate, 
                                                              @Param("endDate") LocalDate endDate);
    
    /**
     * Find leave requests for a specific employee by ID that overlap with a date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    List<LeaveRequest> findOverlappingLeaveRequestsForEmployeeId(@Param("employeeId") Long employeeId,
                                                                @Param("startDate") LocalDate startDate, 
                                                                @Param("endDate") LocalDate endDate);
    
    // ==================== STATUS AND EMPLOYEE COMBINATIONS ====================
    
    /**
     * Find leave requests for a specific employee that start on or after a specific date
     */
    List<LeaveRequest> findByEmployeeAndFromDateAfter(User employee, LocalDate date);
    
    /**
     * Find leave requests for a specific employee by ID that start on or after a specific date
     */
    List<LeaveRequest> findByEmployeeIdAndFromDateAfter(Long employeeId, LocalDate date);
    
    /**
     * Find leave requests for a specific employee that end on or before a specific date
     */
    List<LeaveRequest> findByEmployeeAndToDateBefore(User employee, LocalDate date);
    
    /**
     * Find leave requests for a specific employee by ID that end on or before a specific date
     */
    List<LeaveRequest> findByEmployeeIdAndToDateBefore(Long employeeId, LocalDate date);
    
    /**
     * Find leave requests for a specific employee with a specific status that start on or after a specific date
     */
    List<LeaveRequest> findByEmployeeAndStatusAndFromDateAfter(User employee, LeaveRequestStatus status, LocalDate date);
    
    /**
     * Find leave requests for a specific employee by ID with a specific status that start on or after a specific date
     */
    List<LeaveRequest> findByEmployeeIdAndStatusAndFromDateAfter(Long employeeId, LeaveRequestStatus status, LocalDate date);
    
    // ==================== EXISTENCE CHECKS ====================
    
    /**
     * Check if a leave request exists for a specific employee
     */
    boolean existsByEmployee(User employee);
    
    /**
     * Check if a leave request exists for a specific employee by ID
     */
    boolean existsByEmployeeId(Long employeeId);
    
    /**
     * Check if a leave request exists for a specific employee with a specific status
     */
    boolean existsByEmployeeAndStatus(User employee, LeaveRequestStatus status);
    
    /**
     * Check if a leave request exists for a specific employee by ID with a specific status
     */
    boolean existsByEmployeeIdAndStatus(Long employeeId, LeaveRequestStatus status);
    
    /**
     * Check if there are any overlapping leave requests for a specific employee
     */
    @Query("SELECT CASE WHEN COUNT(lr.id) > 0 THEN true ELSE false END FROM LeaveRequest lr WHERE lr.employee = :employee AND lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    boolean hasOverlappingLeaveRequest(@Param("employee") User employee,
                                      @Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    /**
     * Check if there are any overlapping leave requests for a specific employee by ID
     */
    @Query("SELECT CASE WHEN COUNT(lr.id) > 0 THEN true ELSE false END FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    boolean hasOverlappingLeaveRequestForEmployeeId(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    // ==================== PAGINATION SUPPORT ====================
    
    /**
     * Find all leave requests for a specific employee with pagination
     */
    Page<LeaveRequest> findByEmployee(User employee, Pageable pageable);
    
    /**
     * Find all leave requests for a specific employee by ID with pagination
     */
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);
    
    /**
     * Find all leave requests with a specific status with pagination
     */
    Page<LeaveRequest> findByStatus(LeaveRequestStatus status, Pageable pageable);
    
    /**
     * Find all leave requests for a specific employee with a specific status with pagination
     */
    Page<LeaveRequest> findByEmployeeAndStatus(User employee, LeaveRequestStatus status, Pageable pageable);
    
    /**
     * Find all leave requests for a specific employee by ID with a specific status with pagination
     */
    Page<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequestStatus status, Pageable pageable);
    
    /**
     * Find leave requests that start on or after a specific date with pagination
     */
    Page<LeaveRequest> findByFromDateAfter(LocalDate date, Pageable pageable);
    
    /**
     * Find leave requests that end on or before a specific date with pagination
     */
    Page<LeaveRequest> findByToDateBefore(LocalDate date, Pageable pageable);
    
    /**
     * Find leave requests that start between two dates with pagination
     */
    Page<LeaveRequest> findByFromDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find leave requests that end between two dates with pagination
     */
    Page<LeaveRequest> findByToDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // ==================== STATISTICS AND COUNTS ====================
    
    /**
     * Count leave requests for a specific employee
     */
    long countByEmployee(User employee);
    
    /**
     * Count leave requests for a specific employee by ID
     */
    long countByEmployeeId(Long employeeId);
    
    /**
     * Count leave requests with a specific status
     */
    long countByStatus(LeaveRequestStatus status);
    
    /**
     * Count leave requests for a specific employee with a specific status
     */
    long countByEmployeeAndStatus(User employee, LeaveRequestStatus status);
    
    /**
     * Count leave requests for a specific employee by ID with a specific status
     */
    long countByEmployeeIdAndStatus(Long employeeId, LeaveRequestStatus status);
    
    /**
     * Count leave requests that start on or after a specific date
     */
    long countByFromDateAfter(LocalDate date);
    
    /**
     * Count leave requests that end on or before a specific date
     */
    long countByToDateBefore(LocalDate date);
    
    /**
     * Count leave requests that start between two dates
     */
    long countByFromDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Count leave requests that end between two dates
     */
    long countByToDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Count leave requests in a date range
     */
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    long countLeaveRequestsInDateRange(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    /**
     * Count leave requests for a specific employee in a date range
     */
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee = :employee AND lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    long countLeaveRequestsForEmployeeInDateRange(@Param("employee") User employee,
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    /**
     * Count leave requests for a specific employee by ID in a date range
     */
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    long countLeaveRequestsForEmployeeIdInDateRange(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    // ==================== RECENT LEAVE REQUESTS ====================
    
    /**
     * Find recent leave requests for a specific employee (ordered by from date descending)
     */
    List<LeaveRequest> findTop10ByEmployeeOrderByFromDateDesc(User employee);
    
    /**
     * Find recent leave requests for a specific employee by ID (ordered by from date descending)
     */
    List<LeaveRequest> findTop10ByEmployeeIdOrderByFromDateDesc(Long employeeId);
    
    /**
     * Find recent leave requests with a specific status (ordered by from date descending)
     */
    List<LeaveRequest> findTop10ByStatusOrderByFromDateDesc(LeaveRequestStatus status);
    
    /**
     * Find recent leave requests for a specific employee with a specific status (ordered by from date descending)
     */
    List<LeaveRequest> findTop10ByEmployeeAndStatusOrderByFromDateDesc(User employee, LeaveRequestStatus status);
    
    /**
     * Find recent leave requests for a specific employee by ID with a specific status (ordered by from date descending)
     */
    List<LeaveRequest> findTop10ByEmployeeIdAndStatusOrderByFromDateDesc(Long employeeId, LeaveRequestStatus status);
}
