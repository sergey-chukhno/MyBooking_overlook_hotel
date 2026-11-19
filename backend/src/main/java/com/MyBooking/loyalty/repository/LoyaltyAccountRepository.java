package com.MyBooking.loyalty.repository;

import com.MyBooking.loyalty.domain.LoyaltyAccount;
import com.MyBooking.auth.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {

    // ==================== BASIC QUERIES ====================

    @EntityGraph(attributePaths = {"user"})
    Optional<LoyaltyAccount> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {"user"})
    Optional<LoyaltyAccount> findByPublicIdAndUserId(UUID publicId, Long userId);

    boolean existsByPublicId(UUID publicId);

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<LoyaltyAccount> findAll(Pageable pageable);

    // Find by user (entity-based)
    Optional<LoyaltyAccount> findByUser(User user);
    
    // Find by user (ID-based)
    Optional<LoyaltyAccount> findByUserId(Long userId);

    // Find by user (ID-based) with pessimistic write lock for high-contention operations
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM LoyaltyAccount a WHERE a.user.id = :userId")
    Optional<LoyaltyAccount> findByUserIdForUpdate(@Param("userId") Long userId);

    // ==================== BALANCE QUERIES ====================

    // Find accounts with specific balance
    List<LoyaltyAccount> findByBalance(Integer balance);
    Page<LoyaltyAccount> findByBalance(Integer balance, Pageable pageable);

    // Find accounts with balance greater than
    List<LoyaltyAccount> findByBalanceGreaterThan(Integer balance);
    Page<LoyaltyAccount> findByBalanceGreaterThan(Integer balance, Pageable pageable);

    // Find accounts with balance greater than or equal
    List<LoyaltyAccount> findByBalanceGreaterThanEqual(Integer balance);
    Page<LoyaltyAccount> findByBalanceGreaterThanEqual(Integer balance, Pageable pageable);

    // Find accounts with balance less than
    List<LoyaltyAccount> findByBalanceLessThan(Integer balance);
    Page<LoyaltyAccount> findByBalanceLessThan(Integer balance, Pageable pageable);

    // Find accounts with balance less than or equal
    List<LoyaltyAccount> findByBalanceLessThanEqual(Integer balance);
    Page<LoyaltyAccount> findByBalanceLessThanEqual(Integer balance, Pageable pageable);

    // Find accounts with balance in range
    List<LoyaltyAccount> findByBalanceBetween(Integer minBalance, Integer maxBalance);
    Page<LoyaltyAccount> findByBalanceBetween(Integer minBalance, Integer maxBalance, Pageable pageable);

    // ==================== DATE-BASED QUERIES ====================

    // Find by creation date range
    List<LoyaltyAccount> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<LoyaltyAccount> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by creation date after
    List<LoyaltyAccount> findByCreatedAtAfter(LocalDateTime date);
    Page<LoyaltyAccount> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    // Find by creation date before
    List<LoyaltyAccount> findByCreatedAtBefore(LocalDateTime date);
    Page<LoyaltyAccount> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);

    // ==================== CUSTOM BUSINESS QUERIES ====================

    // Find accounts with zero balance (business logic in repository)
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.balance = 0")
    List<LoyaltyAccount> findAccountsWithZeroBalance();

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.balance = 0")
    Page<LoyaltyAccount> findAccountsWithZeroBalance(Pageable pageable);

    // Find accounts with high balance (business logic in repository)
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.balance >= :threshold ORDER BY la.balance DESC")
    List<LoyaltyAccount> findAccountsWithHighBalance(@Param("threshold") Integer threshold);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT la FROM LoyaltyAccount la WHERE la.balance >= :threshold ORDER BY la.balance DESC")
    Page<LoyaltyAccount> findAccountsWithHighBalance(@Param("threshold") Integer threshold, Pageable pageable);

    // Find accounts ordered by balance (business logic in repository)
    @Query("SELECT la FROM LoyaltyAccount la ORDER BY la.balance DESC")
    List<LoyaltyAccount> findAllOrderByBalanceDesc();

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT la FROM LoyaltyAccount la ORDER BY la.balance DESC")
    Page<LoyaltyAccount> findAllOrderByBalanceDesc(Pageable pageable);

    // Find accounts ordered by creation date (business logic in repository)
    @Query("SELECT la FROM LoyaltyAccount la ORDER BY la.createdAt DESC")
    List<LoyaltyAccount> findAllOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT la FROM LoyaltyAccount la ORDER BY la.createdAt DESC")
    Page<LoyaltyAccount> findAllOrderByCreatedAtDesc(Pageable pageable);

    // ==================== COUNT QUERIES ====================

    // Count by balance
    long countByBalance(Integer balance);
    long countByBalanceGreaterThan(Integer balance);
    long countByBalanceGreaterThanEqual(Integer balance);
    long countByBalanceLessThan(Integer balance);
    long countByBalanceLessThanEqual(Integer balance);
    long countByBalanceBetween(Integer minBalance, Integer maxBalance);

    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBefore(LocalDateTime date);

    // ==================== EXISTENCE QUERIES ====================

    // Check if account exists by user
    boolean existsByUser(User user);
    boolean existsByUserId(Long userId);

    // Check if account exists with specific balance
    boolean existsByBalance(Integer balance);
    boolean existsByBalanceGreaterThan(Integer balance);
    boolean existsByBalanceGreaterThanEqual(Integer balance);
    boolean existsByBalanceLessThan(Integer balance);
    boolean existsByBalanceLessThanEqual(Integer balance);
    boolean existsByBalanceBetween(Integer minBalance, Integer maxBalance);

    // ==================== AGGREGATION QUERIES ====================

    // Get total balance across all accounts (business logic in repository)
    @Query("SELECT COALESCE(SUM(la.balance), 0) FROM LoyaltyAccount la")
    Long getTotalBalance();

    // Get average balance across all accounts (business logic in repository)
    @Query("SELECT COALESCE(AVG(la.balance), 0) FROM LoyaltyAccount la")
    Double getAverageBalance();

    // Get maximum balance (business logic in repository)
    @Query("SELECT COALESCE(MAX(la.balance), 0) FROM LoyaltyAccount la")
    Integer getMaximumBalance();

    // Get minimum balance (business logic in repository)
    @Query("SELECT COALESCE(MIN(la.balance), 0) FROM LoyaltyAccount la")
    Integer getMinimumBalance();

    // Get balance statistics by user role (business logic in repository)
    @Query("SELECT u.role, COALESCE(SUM(la.balance), 0), COALESCE(AVG(la.balance), 0), COUNT(la) " +
           "FROM User u LEFT JOIN LoyaltyAccount la ON u.id = la.user.id " +
           "GROUP BY u.role")
    List<Object[]> getBalanceStatisticsByUserRole();
}