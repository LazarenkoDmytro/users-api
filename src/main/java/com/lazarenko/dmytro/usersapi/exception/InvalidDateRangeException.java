package com.lazarenko.dmytro.usersapi.exception;

/**
 * Indicates an invalid date range where the start date is after the end date.
 *
 * @author Dmytro Lazarenko
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException() {
        super("The 'from' date must be before the 'to' date");
    }
}
