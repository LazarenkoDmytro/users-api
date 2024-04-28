package com.lazarenko.dmytro.usersapi.service;

import com.lazarenko.dmytro.usersapi.dao.UserDAO;
import com.lazarenko.dmytro.usersapi.exception.InvalidDateRangeException;
import com.lazarenko.dmytro.usersapi.exception.MinimumAgeRequirementException;
import com.lazarenko.dmytro.usersapi.exception.UserNotFoundException;
import com.lazarenko.dmytro.usersapi.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles business logic for user management operations.
 *
 * @author Dmytro Lazarenko
 */
@Service
public class UserService {

    private final UserDAO userDAO;

    @Value("${user.minimum-age}")
    private int minimumAge;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Adds a new user after validating their age.
     *
     * @param user the user to add
     * @throws MinimumAgeRequirementException if the user does not meet the minimum age requirement
     * @return the added user
     */
    public User addUser(User user) {
        validateDateOfBirth(user.getDateOfBirth());

        return userDAO.save(user);
    }

    /**
     * Retrieves all users.
     *
     * @return a list of all users
     */
    public List<User> findAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Finds a user by their email.
     *
     * @param email the email to find the user by
     * @return an optional containing the user if found
     * @throws UserNotFoundException if no user is found with the given email
     */
    public User findUserByEmail(String email) {
        Optional<User> user = userDAO.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException(email);
        }

        return user.get();
    }

    /**
     * Finds users born within a specified date range.
     *
     * @param from the start date of the birthdate range
     * @param to   the end date of the birthdate range
     * @return a list of users born between {@code from} and {@code to}
     * @throws InvalidDateRangeException if {@code from} is after {@code to}
     */
    public List<User> findUsersByBirthDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new InvalidDateRangeException();
        }

        return userDAO.findAll().stream()
                .filter(user -> user.getDateOfBirth().isAfter(from) && user.getDateOfBirth().isBefore(to))
                .collect(Collectors.toList());
    }

    /**
     * Updates a user's information.
     *
     * @param email   the email of the user to update
     * @param updates a map containing the updates
     * @return a new user
     * @throws UserNotFoundException          if no user is found with the given email
     * @throws MinimumAgeRequirementException if the user does not meet the minimum age requirement
     */
    public User updateUser(String email, Map<String, Object> updates) {
        Optional<User> user = userDAO.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException(email);
        }

        User foundUser = user.get();
        User newUser = new User(foundUser);
        updates.forEach((key, value) -> {
            switch (key) {
                case "email" -> newUser.setEmail((String) value);
                case "firstName" -> newUser.setFirstName((String) value);
                case "lastName" -> newUser.setLastName((String) value);
                case "dateOfBirth" -> {
                    LocalDate dateOfBirth = LocalDate.parse((String) value);
                    validateDateOfBirth(dateOfBirth);

                    newUser.setDateOfBirth(dateOfBirth);
                }
                case "address" -> newUser.setAddress((String) value);
                case "phoneNumber" -> newUser.setPhoneNumber((String) value);
            }
        });

        return userDAO.update(email, newUser);
    }

    /**
     * Updates a user's information.
     *
     * @param email   the email of the user to update
     * @param newUser a new user to be replaced with
     * @return a new user
     * @throws MinimumAgeRequirementException if the user does not meet the minimum age requirement
     */
    public User replaceUser(String email, User newUser) {
        validateDateOfBirth(newUser.getDateOfBirth());

        return userDAO.update(email, newUser);
    }

    /**
     * Deletes a user by their email.
     *
     * @param email the email of the user to delete
     * @throws UserNotFoundException if no user is found with the given email
     */
    public void deleteUser(String email) {
        if (!userDAO.deleteByEmail(email)) {
            throw new UserNotFoundException(email);
        }
    }

    /**
     * @param dateOfBirth birthdate to be validated
     * @throws MinimumAgeRequirementException if the birthdate does not meet the requirement
     */
    private void validateDateOfBirth(LocalDate dateOfBirth) {
        if (LocalDate.now().minusYears(minimumAge).isBefore(dateOfBirth)) {
            throw new MinimumAgeRequirementException(minimumAge);
        }
    }
}
