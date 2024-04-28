package com.lazarenko.dmytro.usersapi.exception;

/**
 * Indicates that a user was not found in the system.
 *
 * @author Dmytro Lazarenko
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String email) {
        super("Could not find user " + email);
    }
}
