package com.lazarenko.dmytro.usersapi.exception;

/**
 * Indicates that the user does not meet the minimum age requirement.
 *
 * @author Dmytro Lazarenko
 */
public class MinimumAgeRequirementException extends RuntimeException {

    public MinimumAgeRequirementException(int minimumAge) {
        super("User must be at least " + minimumAge + " years old");
    }
}
