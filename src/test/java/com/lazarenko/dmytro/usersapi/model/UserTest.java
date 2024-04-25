package com.lazarenko.dmytro.usersapi.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for User validation.
 *
 * @author Dmytro Lazarenko
 */
class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    @Test
    void missedEmailShouldFailValidation() {
        User user = new User();
        user.setFirstName("Dmytro");
        user.setLastName("Lazarenko");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Email is required", constraintViolations.iterator().next().getMessage());
    }

    @Test
    void invalidEmailShouldFailValidation() {
        User user = new User();
        user.setEmail("invalid-email.com");
        user.setFirstName("Dmytro");
        user.setLastName("Lazarenko");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Invalid email format", constraintViolations.iterator().next().getMessage());
    }

    @Test
    void missedFirstNameShouldFailValidation() {
        User user = new User();
        user.setEmail("my-email@gmail.com");
        user.setLastName("Lazarenko");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("First name is required", constraintViolations.iterator().next().getMessage());
    }

    @Test
    void missedLastNameShouldFailValidation() {
        User user = new User();
        user.setEmail("my-email@gmail.com");
        user.setFirstName("Dmytro");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Last name is required", constraintViolations.iterator().next().getMessage());
    }

    @Test
    void missedDateOfBirthShouldFailValidation() {
        User user = new User();
        user.setEmail("my-email@gmail.com");
        user.setFirstName("Dmytro");
        user.setLastName("Lazarenko");
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Date of birth is required", constraintViolations.iterator().next().getMessage());
    }

    @Test
    void invalidDateOfBirthShouldFailValidation() {
        User user = new User();
        user.setEmail("my-email@gmail.com");
        user.setFirstName("Dmytro");
        user.setLastName("Lazarenko");
        user.setDateOfBirth(LocalDate.of(2025, 1, 1));
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Date of birth must be earlier than current date", constraintViolations.iterator().next().getMessage());
    }

    @Test
    void everythingOkShouldPassValidation() {
        User user = new User();
        user.setEmail("my-email@gmail.com");
        user.setFirstName("Dmytro");
        user.setLastName("Lazarenko");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertTrue(constraintViolations.isEmpty());
    }
}
