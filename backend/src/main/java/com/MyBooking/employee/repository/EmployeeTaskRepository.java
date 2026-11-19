package com.MyBooking.employee.repository;

import com.MyBooking.auth.domain.User;
import com.MyBooking.employee.domain.EmployeeTask;
import com.MyBooking.employee.domain.TaskStatus;
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
public interface EmployeeTaskRepository extends JpaRepository<EmployeeTask, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<EmployeeTask> findByPublicId(UUID publicId);

    Optional<EmployeeTask> findByPublicIdAndEmployeeId(UUID publicId, Long employeeId);

    boolean existsByPublicId(UUID publicId);
    
    /**
     * Find all tasks for a specific employee
     */
    List<EmployeeTask> findByEmployee(User employee);
    
    /**
     * Find all tasks for a specific employee with pagination
     */
    Page<EmployeeTask> findByEmployee(User employee, Pageable pageable);
    
    /**
     * Find all tasks with a specific status
     */
    List<EmployeeTask> findByStatus(TaskStatus status);
    
    /**
     * Find all tasks with a specific status with pagination
     */
    Page<EmployeeTask> findByStatus(TaskStatus status, Pageable pageable);
    
    /**
     * Find all tasks for a specific employee with a specific status
     */
    List<EmployeeTask> findByEmployeeAndStatus(User employee, TaskStatus status);
    
    /**
     * Find all tasks for a specific employee with a specific status with pagination
     */
    Page<EmployeeTask> findByEmployeeAndStatus(User employee, TaskStatus status, Pageable pageable);

    // ==================== TASK TITLE QUERIES ====================
    
    /**
     * Find tasks by title containing text (case-insensitive)
     */
    @Query("SELECT t FROM EmployeeTask t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<EmployeeTask> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    /**
     * Find tasks by title containing text with pagination
     */
    @Query("SELECT t FROM EmployeeTask t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<EmployeeTask> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);
    
    /**
     * Find tasks for a specific employee by title containing text
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.employee = :employee AND LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<EmployeeTask> findByEmployeeAndTitleContainingIgnoreCase(@Param("employee") User employee, @Param("title") String title);

    // ==================== TIME-BASED QUERIES ====================
    
    /**
     * Find tasks created after a specific date
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.createdAt >= :date")
    List<EmployeeTask> findByCreatedAtAfter(@Param("date") LocalDateTime date);
    
    /**
     * Find tasks created before a specific date
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.createdAt <= :date")
    List<EmployeeTask> findByCreatedAtBefore(@Param("date") LocalDateTime date);
    
    /**
     * Find tasks created between two dates
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<EmployeeTask> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find tasks updated after a specific date
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.updatedAt >= :date")
    List<EmployeeTask> findByUpdatedAtAfter(@Param("date") LocalDateTime date);
    
    /**
     * Find tasks updated before a specific date
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.updatedAt <= :date")
    List<EmployeeTask> findByUpdatedAtBefore(@Param("date") LocalDateTime date);
    
    /**
     * Find tasks updated between two dates
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.updatedAt BETWEEN :startDate AND :endDate")
    List<EmployeeTask> findByUpdatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // ==================== COMBINED CRITERIA QUERIES ====================
    
    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT t FROM EmployeeTask t WHERE " +
           "(:employee IS NULL OR t.employee = :employee) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    List<EmployeeTask> findByCriteria(@Param("employee") User employee,
                                     @Param("status") TaskStatus status,
                                     @Param("title") String title);
    
    /**
     * Advanced search with multiple criteria and pagination
     */
    @Query("SELECT t FROM EmployeeTask t WHERE " +
           "(:employee IS NULL OR t.employee = :employee) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<EmployeeTask> findByCriteria(@Param("employee") User employee,
                                     @Param("status") TaskStatus status,
                                     @Param("title") String title,
                                     Pageable pageable);

    // ==================== EXISTENCE CHECKS ====================
    
    /**
     * Check if a task exists for a specific employee
     */
    boolean existsByEmployee(User employee);
    
    /**
     * Check if a task exists for a specific employee with a specific status
     */
    boolean existsByEmployeeAndStatus(User employee, TaskStatus status);

    // ==================== STATISTICS AND COUNTS ====================
    
    /**
     * Count tasks for a specific employee
     */
    long countByEmployee(User employee);
    
    /**
     * Count tasks with a specific status
     */
    long countByStatus(TaskStatus status);
    
    /**
     * Count tasks for a specific employee with a specific status
     */
    long countByEmployeeAndStatus(User employee, TaskStatus status);
    
    /**
     * Count tasks created after a specific date
     */
    long countByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Count tasks created before a specific date
     */
    long countByCreatedAtBefore(LocalDateTime date);
    
    /**
     * Count tasks created between two dates
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count tasks updated after a specific date
     */
    long countByUpdatedAtAfter(LocalDateTime date);
    
    /**
     * Count tasks updated before a specific date
     */
    long countByUpdatedAtBefore(LocalDateTime date);
    
    /**
     * Count tasks updated between two dates
     */
    long countByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ==================== RECENT TASKS ====================
    
    /**
     * Find recent tasks for a specific employee (ordered by created date descending)
     */
    List<EmployeeTask> findTop10ByEmployeeOrderByCreatedAtDesc(User employee);
    
    /**
     * Find recent tasks with a specific status (ordered by created date descending)
     */
    List<EmployeeTask> findTop10ByStatusOrderByCreatedAtDesc(TaskStatus status);
    
    /**
     * Find recent tasks for a specific employee with a specific status (ordered by created date descending)
     */
    List<EmployeeTask> findTop10ByEmployeeAndStatusOrderByCreatedAtDesc(User employee, TaskStatus status);

    // ==================== TASK STATUS TRANSITIONS ====================
    
    /**
     * Find tasks that were updated recently (for status change tracking)
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.updatedAt > t.createdAt AND t.updatedAt >= :date")
    List<EmployeeTask> findRecentlyUpdatedTasks(@Param("date") LocalDateTime date);
    
    /**
     * Find tasks that were completed recently
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.status = 'DONE' AND t.updatedAt >= :date")
    List<EmployeeTask> findRecentlyCompletedTasks(@Param("date") LocalDateTime date);
    
    /**
     * Find tasks that are overdue (TODO or IN_PROGRESS status with old creation date)
     */
    @Query("SELECT t FROM EmployeeTask t WHERE t.status IN ('TODO', 'IN_PROGRESS') AND t.createdAt <= :date")
    List<EmployeeTask> findOverdueTasks(@Param("date") LocalDateTime date);
}
