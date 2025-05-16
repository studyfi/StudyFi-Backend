package com.studyfi.userandgroup.user.exception;

public class EmailVerificationCodeInvalidException extends RuntimeException {

    public EmailVerificationCodeInvalidException(String message) {
        super(message);
    }
}