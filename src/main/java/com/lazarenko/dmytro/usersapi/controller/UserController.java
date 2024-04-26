package com.lazarenko.dmytro.usersapi.controller;

import com.lazarenko.dmytro.usersapi.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();

    @Value("${user.minimum-age}")
    private int minimumAge;

    @GetMapping
    public List<User> all() {
        return users;
    }

    @GetMapping("/{email}")
    public User one(@PathVariable String email) {
        return findUserByEmail(email);
    }

    @PostMapping
    public User newUser(@Valid @RequestBody User user) {
        if (LocalDate.now().minusYears(minimumAge).isBefore(user.getDateOfBirth())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must be at least " + minimumAge + " years old");
        }

        users.add(user);
        return user;
    }

    @PatchMapping("/{email}")
    public User updateUser(@RequestBody Map<String, Object> updates, @PathVariable String email) {
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

    @PutMapping("/{email}")
    public User replaceUser(@RequestBody User newUser, @PathVariable String email) {
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

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> handleDateTimeParseException(HttpServletRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorDetails.put("message", "Please use ISO date format (YYYY-MM-DD).");
        errorDetails.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDetails);
    }

    private User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find user " + email));
    }
}
