package com.lazarenko.dmytro.usersapi.dao;

import com.lazarenko.dmytro.usersapi.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the data access operations managed by {@link UserDAO}.
 *
 * @author Dmytro Lazarenko
 */
public class UserDAOTest {

    private UserDAO userDAO;

    private User user;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setAddress("123 Main St");
        user.setPhoneNumber("123-456-7890");
    }

    @Test
    void testSaveUser() {
        User savedUser = userDAO.save(user);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser).isEqualTo(user);
    }

    @Test
    void testFindAllUsers() {
        userDAO.save(user);
        List<User> users = userDAO.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.getFirst()).isEqualTo(user);
    }

    @Test
    void testFindByEmail() {
        userDAO.save(user);
        Optional<User> foundUser = userDAO.findByEmail("test@example.com");
        assertThat(foundUser.isPresent()).isTrue();
        assertThat(foundUser.get()).isEqualTo(user);
    }

    @Test
    void testUpdateUser() {
        userDAO.save(user);
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setFirstName("Jane");
        newUser.setLastName("Doe");
        newUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        newUser.setAddress("123 Main St");
        newUser.setPhoneNumber("123-456-7890");

        User updatedUser = userDAO.update("test@example.com", newUser);
        assertThat(updatedUser).isEqualTo(user);
    }

    @Test
    void testDeleteUserByEmail() {
        userDAO.save(user);
        boolean result = userDAO.deleteByEmail("test@example.com");
        assertThat(result).isTrue();
        assertThat(userDAO.findAll()).isEmpty();
    }

    @Test
    void testNotFoundByEmail() {
        Optional<User> notFoundUser = userDAO.findByEmail("nonexistent@example.com");
        assertThat(notFoundUser.isPresent()).isFalse();
    }

    @Test
    void testNotUpdateNonexistentUser() {
        User newUser = new User();
        newUser.setEmail("nonexistent@example.com");
        newUser.setFirstName("Test");

        User result = userDAO.update("nonexistent@example.com", newUser);
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("nonexistent@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
    }
}
