package com.lazarenko.dmytro.usersapi.controller;

import com.lazarenko.dmytro.usersapi.model.User;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();

    @GetMapping
    public List<User> all() {
        return users;
    }

    @GetMapping("/{email}")
    public User one(@PathVariable String email) {
        return findUserByEmail(email);
    }

    private User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find user " + email));
    }
}
