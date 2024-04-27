package com.lazarenko.dmytro.usersapi.controller;

import com.lazarenko.dmytro.usersapi.model.User;

import com.lazarenko.dmytro.usersapi.model.assembler.UserModelAssembler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * The UserController class provides RESTful web services for managing users.
 * It includes CRUD operations for user data, filtering users by birthdate range,
 * and handling date format errors in user input.
 *
 * @author Dmytro Lazarenko
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users;

    private final UserModelAssembler assembler;

    @Value("${user.minimum-age}")
    private int minimumAge;

    public UserController(UserModelAssembler assembler) {
        users = new ArrayList<>();
        this.assembler = assembler;
    }

    /**
     * Retrieves all registered users.
     *
     * @return CollectionModel of EntityModel containing all users and associated resources
     */
    @GetMapping
    public CollectionModel<EntityModel<User>> all() {
        List<EntityModel<User>> users = this.users.stream()
                .map(assembler::toModel)
                .toList();

        return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user
     * @return EntityModel containing the user and associated resources
     * @throws ResponseStatusException if no user is found with the given email
     */
    @GetMapping("/{email}")
    public EntityModel<User> one(@PathVariable String email) {
        User user = findUserByEmail(email);

        return assembler.toModel(user);
    }


    /**
     * Creates a new user with validation for minimum age.
     *
     * @param user the user to create
     * @return the created user
     * @throws ResponseStatusException if the user's age is below the minimum required age
     */
    @PostMapping
    public User newUser(@Valid @RequestBody User user) {
        if (LocalDate.now().minusYears(minimumAge).isBefore(user.getDateOfBirth())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must be at least " + minimumAge + " years old");
        }

        users.add(user);
        return user;
    }

    /**
     * Updates existing user details.
     *
     * @param email   the email of the user to update
     * @param updates a map containing user attributes to update
     * @return the updated user
     * @throws ResponseStatusException if no user is found with the given email
     */
    @PatchMapping("/{email}")
    public User updateUser(@PathVariable String email, @RequestBody Map<String, Object> updates) {
        User user = findUserByEmail(email);
        updates.forEach((key, value) -> {
            switch (key) {
                case "email" -> user.setEmail((String) value);
                case "firstName" -> user.setFirstName((String) value);
                case "lastName" -> user.setLastName((String) value);
                case "dateOfBirth" -> user.setDateOfBirth(LocalDate.parse((String) value));
                case "address" -> user.setAddress((String) value);
                case "phoneNumber" -> user.setPhoneNumber((String) value);
            }
        });

        return user;
    }

    /**
     * Replaces an existing user with a new user data.
     *
     * @param email   the email of the user to replace
     * @param newUser the new user data to replace the old one
     * @return the updated user
     */
    @PutMapping("/{email}")
    public User replaceUser(@PathVariable String email, @RequestBody User newUser) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .map(user -> {
                    user.setFirstName(newUser.getFirstName());
                    user.setLastName(newUser.getLastName());
                    user.setDateOfBirth(newUser.getDateOfBirth());
                    user.setPhoneNumber(newUser.getPhoneNumber());
                    user.setAddress(newUser.getAddress());
                    return user;
                })
                .orElseGet(() -> {
                    users.add(newUser);
                    return newUser;
                });
    }

    /**
     * Deletes a user by email.
     *
     * @param email the email of the user to delete
     */
    @DeleteMapping("/{email}")
    public void deleteUser(@PathVariable String email) {
        users.removeIf(user -> user.getEmail().equals(email));
    }

    /**
     * Filters and retrieves users born within a specified date range.
     *
     * @param from the start date of the range
     * @param to   the end date of the range
     * @return a list of users born within the specified range
     * @throws ResponseStatusException if the 'from' date is not before the 'to' date
     */
    @GetMapping("/by-birthdate-range")
    public List<User> usersByBirthDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Past LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Past LocalDate to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The 'from' date must be before the 'to' date");
        }

        return users.stream()
                .filter(user -> (user.getDateOfBirth().isAfter(from) && user.getDateOfBirth().isBefore(to)))
                .toList();
    }

    /**
     * Handles exceptions caused by invalid date format in request parameters.
     *
     * @param request the HttpServletRequest in which the exception occurred
     * @return a ResponseEntity containing the error details
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> handleDateTimeParseException(HttpServletRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorDetails.put("message", "Please use ISO date format (YYYY-MM-DD).");
        errorDetails.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDetails);
    }

    /**
     * Finds a user by email address. Throws a not found exception if no user is found.
     *
     * @param email the email to search for
     * @return the found user
     * @throws ResponseStatusException if no user is found with the given email
     */
    private User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find user " + email));
    }
}
