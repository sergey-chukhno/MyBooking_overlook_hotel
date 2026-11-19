package com.MyBooking.employee.repository;

import com.MyBooking.employee.domain.Employee;
import com.MyBooking.employee.domain.EmployeeStatus;
import com.MyBooking.auth.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<Employee> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);
    
    /**
     * Find employee by user ID
     */
    Optional<Employee> findByUserId(Long userId);

    /**
     * Find employee by user ID with pessimistic write lock to serialize concurrent shift assignments
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e WHERE e.userId = :userId")
    Optional<Employee> findByUserIdForUpdate(@Param("userId") Long userId);
    
    /**
     * Find employee by user
     */
    Optional<Employee> findByUser(User user);
    
    /**
     * Check if employee exists by user ID
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Check if employee exists by user
     */
    boolean existsByUser(User user);

    // ==================== STATUS-BASED QUERIES ====================
    
    /**
     * Find all employees by status
     */
    List<Employee> findByStatus(EmployeeStatus status);
    
    /**
     * Find all employees by status with pagination
     */
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
    
    /**
     * Count employees by status
     */
    long countByStatus(EmployeeStatus status);
    
    /**
     * Find active employees
     */
    @Query("SELECT e FROM Employee e WHERE e.status = 'ACTIVE'")
    List<Employee> findActiveEmployees();
    
    /**
     * Find inactive employees
     */
    @Query("SELECT e FROM Employee e WHERE e.status = 'INACTIVE'")
    List<Employee> findInactiveEmployees();

    // ==================== JOB TITLE QUERIES ====================
    
    /**
     * Find employees by job title
     */
    List<Employee> findByJobTitle(String jobTitle);
    
    /**
     * Find employees by job title with pagination
     */
    Page<Employee> findByJobTitle(String jobTitle, Pageable pageable);
    
    /**
     * Find employees by job title containing text (case-insensitive)
     */
    @Query("SELECT e FROM Employee e WHERE LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Employee> findByJobTitleContainingIgnoreCase(@Param("title") String title);
    
    /**
     * Find employees by job title starting with text (case-insensitive)
     */
    @Query("SELECT e FROM Employee e WHERE LOWER(e.jobTitle) LIKE LOWER(CONCAT(:title, '%'))")
    List<Employee> findByJobTitleStartingWithIgnoreCase(@Param("title") String title);
    
    /**
     * Find all distinct job titles
     */
    @Query("SELECT DISTINCT e.jobTitle FROM Employee e WHERE e.jobTitle IS NOT NULL ORDER BY e.jobTitle")
    List<String> findDistinctJobTitles();
    
    /**
     * Count employees by job title
     */
    long countByJobTitle(String jobTitle);

    // ==================== COMBINED CRITERIA QUERIES ====================
    
    /**
     * Find employees by status and job title
     */
    List<Employee> findByStatusAndJobTitle(EmployeeStatus status, String jobTitle);
    
    /**
     * Find employees by status and job title with pagination
     */
    Page<Employee> findByStatusAndJobTitle(EmployeeStatus status, String jobTitle, Pageable pageable);
    
    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:jobTitle IS NULL OR e.jobTitle LIKE :jobTitle)")
    List<Employee> findByCriteria(@Param("status") EmployeeStatus status,
                                 @Param("jobTitle") String jobTitle);
    
    /**
     * Advanced search with multiple criteria and pagination
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:jobTitle IS NULL OR e.jobTitle LIKE :jobTitle)")
    Page<Employee> findByCriteria(@Param("status") EmployeeStatus status,
                                 @Param("jobTitle") String jobTitle,
                                 Pageable pageable);

    // ==================== TIME-BASED QUERIES ====================
    
    /**
     * Find employees created after a specific date
     */
    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :date")
    List<Employee> findByCreatedAtAfter(@Param("date") LocalDateTime date);
    
    /**
     * Find employees created before a specific date
     */
    @Query("SELECT e FROM Employee e WHERE e.createdAt <= :date")
    List<Employee> findByCreatedAtBefore(@Param("date") LocalDateTime date);
    
    /**
     * Find employees created between two dates
     */
    @Query("SELECT e FROM Employee e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    List<Employee> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find employees updated after a specific date
     */
    @Query("SELECT e FROM Employee e WHERE e.updatedAt >= :date")
    List<Employee> findByUpdatedAtAfter(@Param("date") LocalDateTime date);
    
    /**
     * Find employees updated before a specific date
     */
    @Query("SELECT e FROM Employee e WHERE e.updatedAt <= :date")
    List<Employee> findByUpdatedAtBefore(@Param("date") LocalDateTime date);
    
    /**
     * Find employees updated between two dates
     */
    @Query("SELECT e FROM Employee e WHERE e.updatedAt BETWEEN :startDate AND :endDate")
    List<Employee> findByUpdatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // ==================== USER-RELATED QUERIES ====================
    
    /**
     * Find employees by user's first name
     */
    @Query("SELECT e FROM Employee e JOIN e.user u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))")
    List<Employee> findByUserFirstNameContainingIgnoreCase(@Param("firstName") String firstName);
    
    /**
     * Find employees by user's last name
     */
    @Query("SELECT e FROM Employee e JOIN e.user u WHERE LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Employee> findByUserLastNameContainingIgnoreCase(@Param("lastName") String lastName);
    
    /**
     * Find employees by user's email
     */
    @Query("SELECT e FROM Employee e JOIN e.user u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Employee> findByUserEmailContainingIgnoreCase(@Param("email") String email);
    
    /**
     * Find employees by user's role
     */
    @Query("SELECT e FROM Employee e JOIN e.user u WHERE u.role = :role")
    List<Employee> findByUserRole(@Param("role") com.MyBooking.auth.domain.Role role);

    // ==================== STATISTICS AND ANALYTICS ====================
    
    /**
     * Get total count of employees
     */
    @Query("SELECT COUNT(e) FROM Employee e")
    long getTotalEmployeeCount();
    
    /**
     * Get count of employees by status
     */
    @Query("SELECT e.status, COUNT(e) FROM Employee e GROUP BY e.status")
    List<Object[]> getEmployeeCountByStatus();
    
    /**
     * Get count of employees by job title
     */
    @Query("SELECT e.jobTitle, COUNT(e) FROM Employee e WHERE e.jobTitle IS NOT NULL GROUP BY e.jobTitle ORDER BY COUNT(e) DESC")
    List<Object[]> getEmployeeCountByJobTitle();
    
    /**
     * Get employees created in the last N days
     */
    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :date")
    List<Employee> getEmployeesCreatedInLastDays(@Param("date") LocalDateTime date);
    
    /**
     * Get employees updated in the last N days
     */
    @Query("SELECT e FROM Employee e WHERE e.updatedAt >= :date")
    List<Employee> getEmployeesUpdatedInLastDays(@Param("date") LocalDateTime date);

    // ==================== VALIDATION QUERIES ====================
    
    /**
     * Check if user is already an employee
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e WHERE e.user = :user")
    boolean isUserEmployee(@Param("user") User user);
    
    /**
     * Check if user ID is already an employee
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e WHERE e.userId = :userId")
    boolean isUserIdEmployee(@Param("userId") Long userId);

    /**
     * Find employees currently in IN_PROGRESS training covering the given date
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "JOIN EmployeeTraining et ON et.employee = e.user " +
           "JOIN et.training t " +
           "WHERE et.status = com.MyBooking.employee.domain.TrainingStatus.IN_PROGRESS " +
           "AND t.startDate <= :date AND t.endDate >= :date")
    List<Employee> findEmployeesInTrainingOnDate(@Param("date") java.time.LocalDate date);
}