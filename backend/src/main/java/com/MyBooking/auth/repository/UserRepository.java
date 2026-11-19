package com.MyBooking.auth.repository;

import com.MyBooking.auth.domain.Role;
import com.MyBooking.auth.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * Provides data access methods for authentication, user management, and queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    Optional<User> findByExternalIssuerAndExternalSubject(String externalIssuer, String externalSubject);

    boolean existsByExternalIssuerAndExternalSubject(String externalIssuer, String externalSubject);

    Optional<User> findByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    /**
     * Find user by email address.
     * Used for authentication and user lookup.
     * 
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given email.
     * Used for registration validation.
     * 
     * @param email the email address
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role.
     * Used for role-based queries and admin operations.
     * 
     * @param role the user role
     * @return list of users with the specified role
     */
    List<User> findByRole(Role role);

    /**
     * Find users by role with pagination.
     * Used for admin user management with pagination.
     * 
     * @param role the user role
     * @param pageable pagination information
     * @return page of users with the specified role
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Find users by role and creation date range.
     * Used for analytics and reporting.
     * 
     * @param role the user role
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of users created within the date range
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByRoleAndCreatedAtBetween(
            @Param("role") Role role,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find users by birth date range.
     * Used for age-based queries and analytics.
     * 
     * @param startDate start of birth date range
     * @param endDate end of birth date range
     * @return list of users born within the date range
     */
    List<User> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find users by name pattern (first name or last name).
     * Used for user search functionality.
     * 
     * @param firstNamePattern pattern for first name (case-insensitive)
     * @param lastNamePattern pattern for last name (case-insensitive)
     * @param pageable pagination information
     * @return page of users matching the name patterns
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstNamePattern, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastNamePattern, '%'))")
    Page<User> findByNameContainingIgnoreCase(
            @Param("firstNamePattern") String firstNamePattern,
            @Param("lastNamePattern") String lastNamePattern,
            Pageable pageable
    );

    /**
     * Find users by phone number pattern.
     * Used for user search and validation.
     * 
     * @param phonePattern pattern for phone number
     * @return list of users with matching phone numbers
     */
    List<User> findByPhoneContaining(String phonePattern);

    /**
     * Find users created within a date range.
     * Used for analytics and reporting.
     * 
     * @param startDate start of creation date range
     * @param endDate end of creation date range
     * @param pageable pagination information
     * @return page of users created within the date range
     */
    Page<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Count users by role.
     * Used for dashboard statistics.
     * 
     * @param role the user role
     * @return count of users with the specified role
     */
    long countByRole(Role role);

    /**
     * Find users with specific roles.
     * Used for multi-role queries.
     * 
     * @param roles list of roles to search for
     * @return list of users with any of the specified roles
     */
    List<User> findByRoleIn(List<Role> roles);

    /**
     * Find users with specific roles and pagination.
     * Used for admin user management with multiple role filtering.
     * 
     * @param roles list of roles to search for
     * @param pageable pagination information
     * @return page of users with any of the specified roles
     */
    Page<User> findByRoleIn(List<Role> roles, Pageable pageable);

    /**
     * Find users by email domain.
     * Used for organization-based queries.
     * 
     * @param domain the email domain (e.g., "company.com")
     * @return list of users with emails from the specified domain
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%', :domain)")
    List<User> findByEmailDomain(@Param("domain") String domain);

    /**
     * Find recently created users.
     * Used for new user notifications and analytics.
     * 
     * @param sinceDate users created after this date
     * @param pageable pagination information
     * @return page of recently created users
     */
    Page<User> findByCreatedAtAfter(LocalDateTime sinceDate, Pageable pageable);

    /**
     * Find users by address pattern.
     * Used for location-based queries.
     * 
     * @param addressPattern pattern for address (case-insensitive)
     * @return list of users with matching addresses
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.address) LIKE LOWER(CONCAT('%', :addressPattern, '%'))")
    List<User> findByAddressContainingIgnoreCase(@Param("addressPattern") String addressPattern);

    /**
     * Find users with multiple criteria.
     * Used for advanced search functionality.
     * 
     * @param role the user role (optional)
     * @param firstNamePattern pattern for first name (optional)
     * @param lastNamePattern pattern for last name (optional)
     * @param pageable pagination information
     * @return page of users matching the criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:firstNamePattern IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstNamePattern, '%'))) AND " +
           "(:lastNamePattern IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastNamePattern, '%')))")
    Page<User> findByCriteria(
            @Param("role") Role role,
            @Param("firstNamePattern") String firstNamePattern,
            @Param("lastNamePattern") String lastNamePattern,
            Pageable pageable
    );
}
