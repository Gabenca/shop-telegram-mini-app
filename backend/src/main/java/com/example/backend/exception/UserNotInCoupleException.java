package com.example.backend.exception;

public class UserNotInCoupleException extends RuntimeException {
    public UserNotInCoupleException(String message) {
        super(message);
    }
}
