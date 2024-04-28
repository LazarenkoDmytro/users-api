package com.lazarenko.dmytro.usersapi.dao;

import com.lazarenko.dmytro.usersapi.model.User;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserDAO handles the data access operations for User entities.
 *
 * @author Dmytro Lazarenko
 */
@Repository
public class UserDAO {

    private final List<User> users = new ArrayList<>();

    /**
     * Saves a new user to the storage.
     *
     * @param user the user to save
     * @return the saved user
     */
    public User save(User user) {
        users.add(user);

        return user;
    }

    /**
     * Retrieves all users from the storage.
     *
     * @return a list of all users
     */
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an optional user if found
     */
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    /**
     * Updates an existing user.
     *
     * @param email   the email of the user to update
     * @param newUser the user with updated information
     * @return a new user
     */
    public User update(String email, User newUser) {
        return findByEmail(email)
                .map(user -> {
                    user.setEmail(newUser.getEmail());
                    user.setFirstName(newUser.getFirstName());
                    user.setLastName(newUser.getLastName());
                    user.setDateOfBirth(newUser.getDateOfBirth());
                    user.setAddress(newUser.getAddress());
                    user.setPhoneNumber(newUser.getPhoneNumber());
                    return user;
                })
                .orElseGet(() -> {
                    save(newUser);
                    return newUser;
                });
    }

    /**
     * Deletes a user by their email address.
     *
     * @param email the email address of the user to delete
     * @return true if a user was deleted, false otherwise
     */
    public boolean deleteByEmail(String email) {
        return users.removeIf(user -> user.getEmail().equals(email));
    }
}
