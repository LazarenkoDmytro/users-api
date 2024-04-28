package com.lazarenko.dmytro.usersapi.controller;

import com.lazarenko.dmytro.usersapi.exception.InvalidDateRangeException;
import com.lazarenko.dmytro.usersapi.exception.MinimumAgeRequirementException;
import com.lazarenko.dmytro.usersapi.exception.UserNotFoundException;
import com.lazarenko.dmytro.usersapi.model.User;

import com.lazarenko.dmytro.usersapi.model.assembler.UserModelAssembler;
import com.lazarenko.dmytro.usersapi.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * The UserController class provides RESTful web services for managing users.
 *
 * @author Dmytro Lazarenko
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final UserModelAssembler assembler;

    @Autowired
    public UserController(UserService userService, UserModelAssembler assembler) {
        this.userService = userService;
        this.assembler = assembler;
    }

    /**
     * Creates a new user with validation for minimum age.
     *
     * @param user the user to create
     * @return ResponseEntity containing the created user and associated resources
     * @throws MinimumAgeRequirementException if the user does not meet the minimum age requirement
     */
    @PostMapping
    public ResponseEntity<EntityModel<User>> newUser(@Valid @RequestBody User user) {
        EntityModel<User> userModel = assembler.toModel(userService.addUser(user));

        return ResponseEntity
                .created(userModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(userModel);
    }

    /**
     * Retrieves all registered users.
     *
     * @return CollectionModel of EntityModel containing all users and associated resources
     */
    @GetMapping
    public CollectionModel<EntityModel<User>> all() {
        List<EntityModel<User>> users = userService.findAllUsers().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user
     * @return EntityModel containing the user and associated resources
     * @throws UserNotFoundException if no user is found with the given email
     */
    @GetMapping("/{email}")
    public EntityModel<User> one(@PathVariable String email) {
        return assembler.toModel(userService.findUserByEmail(email));
    }

    /**
     * Filters and retrieves users born within a specified date range.
     *
     * @param from the start date of the range
     * @param to   the end date of the range
     * @return CollectionModel of EntityModel containing users born within the specified range and associated resources
     * @throws InvalidDateRangeException if the 'from' date is after the 'to' date
     */
    @GetMapping("/by-birthdate-range")
    public CollectionModel<EntityModel<User>> usersByBirthDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Past LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Past LocalDate to) {
        List<EntityModel<User>> users = userService.findUsersByBirthDateRange(from, to).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(UserController.class).usersByBirthDateRange(from, to)).withSelfRel());
    }

    /**
     * Updates existing user details.
     *
     * @param email   the email of the user to update
     * @param updates a map containing user attributes to update
     * @return ResponseEntity containing the updated user and associated resources
     * @throws UserNotFoundException if no user is found with the given email
     * @throws MinimumAgeRequirementException if the updated date of birth does not meet the minimum age requirement
     */
    @PatchMapping("/{email}")
    public ResponseEntity<EntityModel<User>> updateUser(@PathVariable String email, @RequestBody Map<String, Object> updates) {
        EntityModel<User> userModel = assembler.toModel(userService.updateUser(email, updates));

        return ResponseEntity
                .ok()
                .location(userModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(userModel);
    }

    /**
     * Replaces an existing user with a new user data.
     *
     * @param email   the email of the user to replace
     * @param newUser the new user data to replace the old one
     * @return ResponseEntity containing the replaced user and associated resources
     * @throws UserNotFoundException if no user is found with the given email
     * @throws MinimumAgeRequirementException if the new user data does not meet the minimum age requirement
     */
    @PutMapping("/{email}")
    public ResponseEntity<EntityModel<User>> replaceUser(@PathVariable String email, @Valid @RequestBody User newUser) {
        EntityModel<User> userModel = assembler.toModel(userService.replaceUser(email, newUser));

        return ResponseEntity
                .created(userModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(userModel);
    }

    /**
     * Deletes a user by email.
     *
     * @param email the email of the user to delete
     * @return ResponseEntity indicating the operation's success
     * @throws UserNotFoundException if no user is found with the given email
     */
    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);

        return ResponseEntity.noContent().build();
    }

    /**
     * Handles exceptions caused by invalid date format in request parameters.
     *
     * @return a ResponseEntity containing the error details
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> handleDateTimeParseException() {

        return handleException("Please use ISO date format (YYYY-MM-DD).", HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles exceptions caused by non-existent user in request parameters.
     *
     * @param ex The handled exception
     * @return a ResponseEntity containing the error details
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {

        return handleException(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles exceptions caused by invalid order of dates or breaking the minimum age requirement in request parameters.
     *
     * @param ex The handled exception
     * @return a ResponseEntity containing the error details
     */
    @ExceptionHandler({InvalidDateRangeException.class, MinimumAgeRequirementException.class})
    public ResponseEntity<Map<String, Object>> handleInvalidDateRangeException(Exception ex) {

        return handleException(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Returns the template for exception response.
     *
     * @return a ResponseEntity containing the error details
     */
    private ResponseEntity<Map<String, Object>> handleException(String message, HttpStatus status) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);

        return ResponseEntity
                .status(status)
                .body(errorDetails);
    }
}
