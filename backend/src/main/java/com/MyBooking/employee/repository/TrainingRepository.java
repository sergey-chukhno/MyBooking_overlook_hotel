package com.MyBooking.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.MyBooking.employee.domain.Training;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    // Basic queries
    Optional<Training> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);
    List<Training> findByTitle(String title);
    List<Training> findByTitleContaining(String keyword);
    
    // Date-based queries
    List<Training> findByStartDate(LocalDate startDate);
    List<Training> findByEndDate(LocalDate endDate);
    List<Training> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    List<Training> findByEndDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Date range queries
    List<Training> findByStartDateAfter(LocalDate startDate);
    List<Training> findByEndDateBefore(LocalDate endDate);
    
    // Overlap detection
    @Query("SELECT t FROM Training t WHERE t.startDate <= :endDate AND t.endDate >= :startDate")
    List<Training> findOverlappingTrainings(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    // Check if a specific training overlaps with others
    @Query("SELECT t FROM Training t WHERE t.id != :trainingId AND t.startDate <= :endDate AND t.endDate >= :startDate")
    List<Training> findOverlappingTrainingsExcluding(@Param("trainingId") Long trainingId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    // Existence checks
    boolean existsByTitle(String title);
    boolean existsByTitleContaining(String keyword);
    
    // Pagination support
    Page<Training> findByTitleContaining(String keyword, Pageable pageable);
    Page<Training> findByStartDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Statistics
    long countByStartDateAfter(LocalDate startDate);
    long countByEndDateBefore(LocalDate endDate);


    // Additional existence checks
    boolean existsByStartDate(LocalDate startDate);
    boolean existsByEndDate(LocalDate endDate);

    // Additional pagination methods
    Page<Training> findByStartDateAfter(LocalDate startDate, Pageable pageable);
    Page<Training> findByEndDateBefore(LocalDate endDate, Pageable pageable);

        // Additional count methods
    long countByStartDateBetween(LocalDate startDate, LocalDate endDate);
    long countByEndDateBetween(LocalDate startDate, LocalDate endDate);
    long countByTitleContaining(String keyword);
}