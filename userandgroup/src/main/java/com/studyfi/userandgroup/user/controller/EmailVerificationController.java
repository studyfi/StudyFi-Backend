package com.studyfi.userandgroup.user.controller;

import com.studyfi.userandgroup.user.dto.EmailRequestDTO;
import com.studyfi.userandgroup.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/email-verification")
public class EmailVerificationController {

    private final UserService userService;

    @Autowired
    public EmailVerificationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/send-email-code")
    public ResponseEntity<?> sendEmailVerificationCode(@RequestBody EmailRequestDTO emailRequest) {
        try {
            userService.sendEmailVerificationCode(emailRequest.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Verification code sent to your email.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while sending the verification code.");
        }
    }

    @PostMapping("/validate-email-code")
    public ResponseEntity<?> validateEmailVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        if (email == null || code == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and code are required.");
        }

        try {
            userService.validateEmailVerificationCode(email, code);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email verified successfully.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while validating the verification code.");
        }
    }
}