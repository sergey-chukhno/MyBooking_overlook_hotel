package com.MyBooking.loyalty.repository;

import com.MyBooking.loyalty.domain.LoyaltyTransaction;
import com.MyBooking.loyalty.domain.LoyaltyAccount;
import com.MyBooking.loyalty.domain.LoyaltyTxType;
import com.MyBooking.reservation.domain.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    // ==================== BASIC QUERIES ====================

    @EntityGraph(attributePaths = {"account", "account.user"})
    Optional<LoyaltyTransaction> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    @Override
    @EntityGraph(attributePaths = {"account", "account.user"})
    Page<LoyaltyTransaction> findAll(Pageable pageable);

    // Find by account (entity-based)
    List<LoyaltyTransaction> findByAccount(LoyaltyAccount account);
    @EntityGraph(attributePaths = {"account", "account.user"})
    Page<LoyaltyTransaction> findByAccount(LoyaltyAccount account, Pageable pageable);

    // Find by account (ID-based)
    List<LoyaltyTransaction> findByAccountId(Long accountId);
    @EntityGraph(attributePaths = {"account", "account.user"})
    Page<LoyaltyTransaction> findByAccountId(Long accountId, Pageable pageable);
    List<LoyaltyTransaction> findByAccountIdAndCreatedAtAfterOrderByCreatedAtDesc(Long accountId, LocalDateTime since);

    // Find by transaction type
    List<LoyaltyTransaction> findByType(LoyaltyTxType type);
    Page<LoyaltyTransaction> findByType(LoyaltyTxType type, Pageable pageable);

    // Find by reservation (entity-based)
    List<LoyaltyTransaction> findByReservation(Reservation reservation);
    Page<LoyaltyTransaction> findByReservation(Reservation reservation, Pageable pageable);

    // Find by reservation (ID-based)
    List<LoyaltyTransaction> findByReservationId(Long reservationId);
    Page<LoyaltyTransaction> findByReservationId(Long reservationId, Pageable pageable);

    // ==================== POINTS QUERIES ====================

    // Find by points amount
    List<LoyaltyTransaction> findByPoints(Integer points);
    Page<LoyaltyTransaction> findByPoints(Integer points, Pageable pageable);

    // Find by points greater than
    List<LoyaltyTransaction> findByPointsGreaterThan(Integer points);
    Page<LoyaltyTransaction> findByPointsGreaterThan(Integer points, Pageable pageable);

    // Find by points greater than or equal
    List<LoyaltyTransaction> findByPointsGreaterThanEqual(Integer points);
    Page<LoyaltyTransaction> findByPointsGreaterThanEqual(Integer points, Pageable pageable);

    // Find by points less than
    List<LoyaltyTransaction> findByPointsLessThan(Integer points);
    Page<LoyaltyTransaction> findByPointsLessThan(Integer points, Pageable pageable);

    // Find by points less than or equal
    List<LoyaltyTransaction> findByPointsLessThanEqual(Integer points);
    Page<LoyaltyTransaction> findByPointsLessThanEqual(Integer points, Pageable pageable);

    // Find by points in range
    List<LoyaltyTransaction> findByPointsBetween(Integer minPoints, Integer maxPoints);
    Page<LoyaltyTransaction> findByPointsBetween(Integer minPoints, Integer maxPoints, Pageable pageable);

    // ==================== DATE-BASED QUERIES ====================

    // Find by creation date range
    List<LoyaltyTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<LoyaltyTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by creation date after
    List<LoyaltyTransaction> findByCreatedAtAfter(LocalDateTime date);
    Page<LoyaltyTransaction> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    // Find by creation date before
    List<LoyaltyTransaction> findByCreatedAtBefore(LocalDateTime date);
    Page<LoyaltyTransaction> findByCreatedAtBefore(LocalDateTime date, Pageable pageable);

    // ==================== COMBINED QUERIES ====================

    // Find by account and type
    List<LoyaltyTransaction> findByAccountAndType(LoyaltyAccount account, LoyaltyTxType type);
    Page<LoyaltyTransaction> findByAccountAndType(LoyaltyAccount account, LoyaltyTxType type, Pageable pageable);
    List<LoyaltyTransaction> findByAccountIdAndType(Long accountId, LoyaltyTxType type);
    Page<LoyaltyTransaction> findByAccountIdAndType(Long accountId, LoyaltyTxType type, Pageable pageable);

    // Find by account and reservation
    List<LoyaltyTransaction> findByAccountAndReservation(LoyaltyAccount account, Reservation reservation);
    Page<LoyaltyTransaction> findByAccountAndReservation(LoyaltyAccount account, Reservation reservation, Pageable pageable);
    List<LoyaltyTransaction> findByAccountIdAndReservationId(Long accountId, Long reservationId);
    Page<LoyaltyTransaction> findByAccountIdAndReservationId(Long accountId, Long reservationId, Pageable pageable);

    // Find by type and reservation
    List<LoyaltyTransaction> findByTypeAndReservation(LoyaltyTxType type, Reservation reservation);
    Page<LoyaltyTransaction> findByTypeAndReservation(LoyaltyTxType type, Reservation reservation, Pageable pageable);
    List<LoyaltyTransaction> findByTypeAndReservationId(LoyaltyTxType type, Long reservationId);
    Page<LoyaltyTransaction> findByTypeAndReservationId(LoyaltyTxType type, Long reservationId, Pageable pageable);

    // ==================== CUSTOM BUSINESS QUERIES ====================

    // Find recent transactions (business logic in repository)
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.createdAt >= :since ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findRecentTransactions(@Param("since") LocalDateTime since);

    @EntityGraph(attributePaths = {"account", "account.user"})
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.createdAt >= :since ORDER BY lt.createdAt DESC")
    Page<LoyaltyTransaction> findRecentTransactions(@Param("since") LocalDateTime since, Pageable pageable);

    // Find transactions by account ordered by creation date (business logic in repository)
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.account = :account ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findByAccountOrderByCreatedAtDesc(@Param("account") LoyaltyAccount account);

    @EntityGraph(attributePaths = {"account", "account.user"})
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.account = :account ORDER BY lt.createdAt DESC")
    Page<LoyaltyTransaction> findByAccountOrderByCreatedAtDesc(@Param("account") LoyaltyAccount account, Pageable pageable);

    // Find transactions by account ID ordered by creation date (business logic in repository)
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.account.id = :accountId ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId);

    @EntityGraph(attributePaths = {"account", "account.user"})
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.account.id = :accountId ORDER BY lt.createdAt DESC")
    Page<LoyaltyTransaction> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);

    // Find transactions by type ordered by creation date (business logic in repository)
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.type = :type ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findByTypeOrderByCreatedAtDesc(@Param("type") LoyaltyTxType type);

    @EntityGraph(attributePaths = {"account", "account.user"})
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.type = :type ORDER BY lt.createdAt DESC")
    Page<LoyaltyTransaction> findByTypeOrderByCreatedAtDesc(@Param("type") LoyaltyTxType type, Pageable pageable);

    // Find high-value transactions (business logic in repository)
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.points >= :threshold ORDER BY lt.points DESC")
    List<LoyaltyTransaction> findHighValueTransactions(@Param("threshold") Integer threshold);

    @EntityGraph(attributePaths = {"account", "account.user"})
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.points >= :threshold ORDER BY lt.points DESC")
    Page<LoyaltyTransaction> findHighValueTransactions(@Param("threshold") Integer threshold, Pageable pageable);

    // Find transactions without reservation (business logic in repository)
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.reservation IS NULL")
    List<LoyaltyTransaction> findTransactionsWithoutReservation();

    @EntityGraph(attributePaths = {"account", "account.user"})
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.reservation IS NULL")
    Page<LoyaltyTransaction> findTransactionsWithoutReservation(Pageable pageable);

    // ==================== COUNT QUERIES ====================

    // Count by account
    long countByAccount(LoyaltyAccount account);
    long countByAccountId(Long accountId);

    // Count by type
    long countByType(LoyaltyTxType type);

    // Count by reservation
    long countByReservation(Reservation reservation);
    long countByReservationId(Long reservationId);

    // Count by points
    long countByPoints(Integer points);
    long countByPointsGreaterThan(Integer points);
    long countByPointsGreaterThanEqual(Integer points);
    long countByPointsLessThan(Integer points);
    long countByPointsLessThanEqual(Integer points);
    long countByPointsBetween(Integer minPoints, Integer maxPoints);

    // Count by creation date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBefore(LocalDateTime date);

    // Count by combined criteria
    long countByAccountAndType(LoyaltyAccount account, LoyaltyTxType type);
    long countByAccountIdAndType(Long accountId, LoyaltyTxType type);
    long countByAccountAndReservation(LoyaltyAccount account, Reservation reservation);
    long countByAccountIdAndReservationId(Long accountId, Long reservationId);
    long countByTypeAndReservation(LoyaltyTxType type, Reservation reservation);
    long countByTypeAndReservationId(LoyaltyTxType type, Long reservationId);

    // ==================== EXISTENCE QUERIES ====================

    // Check if transaction exists by account
    boolean existsByAccount(LoyaltyAccount account);
    boolean existsByAccountId(Long accountId);

    // Check if transaction exists by type
    boolean existsByType(LoyaltyTxType type);

    // Check if transaction exists by reservation
    boolean existsByReservation(Reservation reservation);
    boolean existsByReservationId(Long reservationId);

    // Check if transaction exists by combined criteria
    boolean existsByAccountAndType(LoyaltyAccount account, LoyaltyTxType type);
    boolean existsByAccountIdAndType(Long accountId, LoyaltyTxType type);
    boolean existsByAccountAndReservation(LoyaltyAccount account, Reservation reservation);
    boolean existsByAccountIdAndReservationId(Long accountId, Long reservationId);
    boolean existsByTypeAndReservation(LoyaltyTxType type, Reservation reservation);
    boolean existsByTypeAndReservationId(LoyaltyTxType type, Long reservationId);

    // ==================== AGGREGATION QUERIES ====================

    // Get total points by account (business logic in repository)
    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.account = :account AND lt.type = :type")
    Long getTotalPointsByAccountAndType(@Param("account") LoyaltyAccount account, @Param("type") LoyaltyTxType type);

    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.account.id = :accountId AND lt.type = :type")
    Long getTotalPointsByAccountIdAndType(@Param("accountId") Long accountId, @Param("type") LoyaltyTxType type);

    // Get total points by type (business logic in repository)
    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.type = :type")
    Long getTotalPointsByType(@Param("type") LoyaltyTxType type);

    // Get transaction count by type (business logic in repository)
    @Query("SELECT lt.type, COUNT(lt) FROM LoyaltyTransaction lt GROUP BY lt.type")
    List<Object[]> getTransactionCountByType();

    // Get points statistics by type (business logic in repository)
    @Query("SELECT lt.type, COALESCE(SUM(lt.points), 0), COALESCE(AVG(lt.points), 0), COUNT(lt) FROM LoyaltyTransaction lt GROUP BY lt.type")
    List<Object[]> getPointsStatisticsByType();

    // Get latest transaction by account (business logic in repository)
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.account = :account ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findLatestTransactionByAccount(@Param("account") LoyaltyAccount account, Pageable pageable);

    // Get latest transaction by account ID (business logic in repository)
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.account.id = :accountId ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findLatestTransactionByAccountId(@Param("accountId") Long accountId, Pageable pageable);
}