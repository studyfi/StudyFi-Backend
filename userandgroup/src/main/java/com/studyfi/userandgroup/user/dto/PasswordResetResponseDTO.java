package com.studyfi.userandgroup.user.dto;

public class PasswordResetResponseDTO {
    private String message;
    private String verificationCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}