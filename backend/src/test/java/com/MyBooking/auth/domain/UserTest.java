package com.MyBooking.auth.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");
        user.setPhone("+1234567890");
        user.setAddress("123 Main St, City, Country");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setRole(Role.CLIENT);
    }

    @Test
    void testValidUser() {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should have no violations");
    }

    @Test
    void testFirstNameValidation() {
        // Test null first name
        user.setFirstName(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("First name is required")));

        // Test blank first name
        user.setFirstName("");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        // Test too long first name
        user.setFirstName("A".repeat(51));
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not exceed 50 characters")));
    }

    @Test
    void testEmailValidation() {
        // Test invalid email format
        user.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email should be valid")));

        // Test null email
        user.setEmail(null);
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testPasswordValidation() {
        // Test short password
        user.setPassword("123");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("at least 8 characters")));
    }

    @Test
    void testPhoneValidation() {
        // Test invalid phone format
        user.setPhone("invalid-phone");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Phone number should be valid")));
    }

    @Test
    void testBirthDateValidation() {
        // Test null birth date
        user.setBirthDate(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Birth date is required")));
    }

    @Test
    void testRoleValidation() {
        // Test null role
        user.setRole(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testUtilityMethods() {
        // Test role checking methods
        user.setRole(Role.CLIENT);
        assertTrue(user.isClient());
        assertFalse(user.isEmployee());
        assertFalse(user.isAdmin());

        user.setRole(Role.EMPLOYEE);
        assertFalse(user.isClient());
        assertTrue(user.isEmployee());
        assertFalse(user.isAdmin());

        user.setRole(Role.ADMIN);
        assertFalse(user.isClient());
        assertFalse(user.isEmployee());
        assertTrue(user.isAdmin());
    }

    @Test
    void testGetFullName() {
        user.setFirstName("John");
        user.setLastName("Doe");
        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void testConstructor() {
        User newUser = new User("Jane", "Smith", "jane@example.com", "password123",
                "+9876543210", "456 Oak St", LocalDate.of(1985, 5, 15), Role.EMPLOYEE);
        
        assertEquals("Jane", newUser.getFirstName());
        assertEquals("Smith", newUser.getLastName());
        assertEquals("jane@example.com", newUser.getEmail());
        assertEquals("password123", newUser.getPassword());
        assertEquals("+9876543210", newUser.getPhone());
        assertEquals("456 Oak St", newUser.getAddress());
        assertEquals(LocalDate.of(1985, 5, 15), newUser.getBirthDate());
        assertEquals(Role.EMPLOYEE, newUser.getRole());
        assertTrue(newUser.isEnabled());
        assertFalse(newUser.isEmailVerified());
    }

    @Test
    void testExternalIdentityAndNullablePasswordDefaults() {
        // IDP-T03: Keycloak JIT-provisioned or migrated users have null password and paired (externalIssuer, externalSubject)
        user.setPassword(null);
        user.setExternalIssuer("http://127.0.0.1:8180/realms/mybooking");
        user.setExternalSubject("00000000-0000-4000-a000-000000000003");
        user.setEmailVerified(true);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "User with null password and external identity should pass bean validation");
        assertEquals("http://127.0.0.1:8180/realms/mybooking", user.getExternalIssuer());
        assertEquals("00000000-0000-4000-a000-000000000003", user.getExternalSubject());
        assertTrue(user.isEmailVerified());
        assertTrue(user.isEnabled());
    }
}