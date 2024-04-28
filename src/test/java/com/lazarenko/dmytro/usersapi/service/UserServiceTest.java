package com.lazarenko.dmytro.usersapi.service;

import com.lazarenko.dmytro.usersapi.dao.UserDAO;
import com.lazarenko.dmytro.usersapi.exception.InvalidDateRangeException;
import com.lazarenko.dmytro.usersapi.exception.MinimumAgeRequirementException;
import com.lazarenko.dmytro.usersapi.exception.UserNotFoundException;
import com.lazarenko.dmytro.usersapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the service layer operations managed by {@link UserService}.
 *
 * @author Dmytro lazarenko
 */
@SpringBootTest
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDateOfBirth(LocalDate.now().minusYears(30)); // Valid age
        ReflectionTestUtils.setField(userService, "minimumAge", 18);
    }

    @Test
    void addUser_ValidUser_ReturnsSavedUser() {
        when(userDAO.save(any(User.class))).thenReturn(user);
        User savedUser = userService.addUser(user);
        assertThat(savedUser).isNotNull();
        verify(userDAO).save(user);
    }

    @Test
    void addUser_YoungUser_ThrowsMinimumAgeRequirementException() {
        user.setDateOfBirth(LocalDate.now().minusYears(10)); // Not old enough
        assertThatThrownBy(() -> userService.addUser(user))
                .isInstanceOf(MinimumAgeRequirementException.class)
                .hasMessageContaining("User must be at least");
    }

    @Test
    void findAllUsers_ReturnsListOfUsers() {
        when(userDAO.findAll()).thenReturn(Collections.singletonList(user));
        List<User> users = userService.findAllUsers();
        assertThat(users).containsExactly(user);
    }

    @Test
    void findUserByEmail_ExistingEmail_ReturnsUser() {
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        User foundUser = userService.findUserByEmail("test@example.com");
        assertThat(foundUser).isEqualTo(user);
    }

    @Test
    void findUserByEmail_NonExistingEmail_ThrowsUserNotFoundException() {
        when(userDAO.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findUserByEmail("nonexistent@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Could not find user");
    }

    @Test
    void findUsersByBirthDateRange_ValidRange_ReturnsUsers() {
        when(userDAO.findAll()).thenReturn(Collections.singletonList(user));
        List<User> users = userService.findUsersByBirthDateRange(LocalDate.now().minusYears(40), LocalDate.now().minusYears(20));
        assertThat(users).containsExactly(user);
    }

    @Test
    void findUsersByBirthDateRange_InvalidRange_ThrowsInvalidDateRangeException() {
        assertThatThrownBy(() -> userService.findUsersByBirthDateRange(LocalDate.now(), LocalDate.now().minusYears(1)))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("The 'from' date must be before the 'to' date");
    }

    @Test
    void updateUser_ExistingUser_ReturnsUpdatedUser() {
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFirstName("Jane");
        newUser.setLastName(user.getLastName());
        newUser.setDateOfBirth(user.getDateOfBirth());

        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userDAO.update(eq("test@example.com"), any(User.class))).thenReturn(newUser);
        User updatedUser = userService.updateUser("test@example.com", Collections.singletonMap("firstName", "Jane"));
        assertThat(updatedUser.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void updateUser_NonExistingUser_ThrowsUserNotFoundException() {
        when(userDAO.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser("nonexistent@example.com", Collections.singletonMap("firstName", "Jane")))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Could not find user");
    }

    @Test
    void replaceUser_ExistingUser_ReturnsReplacedUser() {
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFirstName("Jane");
        newUser.setLastName(user.getLastName());
        newUser.setDateOfBirth(user.getDateOfBirth());

        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userDAO.update(eq("test@example.com"), any(User.class))).thenReturn(newUser);
        User updatedUser = userService.replaceUser("test@example.com", newUser);
        assertThat(updatedUser).isEqualTo(newUser);
    }

    @Test
    void replaceUser_NonExistingUser_CreatesUser() {
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFirstName("Jane");
        newUser.setLastName(user.getLastName());
        newUser.setDateOfBirth(user.getDateOfBirth());

        when(userDAO.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        when(userDAO.update(eq("nonexistent@example.com"), any(User.class))).thenReturn(newUser);
        User updatedUser = userService.replaceUser("nonexistent@example.com", newUser);
        assertThat(updatedUser).isEqualTo(newUser);
    }

    @Test
    void deleteUser_ExistingEmail_DeletesUser() {
        when(userDAO.deleteByEmail("test@example.com")).thenReturn(true);
        userService.deleteUser("test@example.com");
        verify(userDAO).deleteByEmail("test@example.com");
    }

    @Test
    void deleteUser_NonExistingEmail_ThrowsUserNotFoundException() {
        when(userDAO.deleteByEmail("nonexistent@example.com")).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser("nonexistent@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Could not find user");
    }
}
