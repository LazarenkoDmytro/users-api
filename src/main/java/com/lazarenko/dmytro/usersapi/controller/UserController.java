package com.lazarenko.dmytro.usersapi.controller;

import com.lazarenko.dmytro.usersapi.model.User;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    private User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find user " + email));
    }
}
