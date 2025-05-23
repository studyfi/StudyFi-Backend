package com.studyfi.userandgroup.user.service;

import com.studyfi.userandgroup.group.model.Group;
import com.studyfi.userandgroup.user.dto.PasswordResetResponseDTO;
import com.studyfi.userandgroup.user.dto.PasswordResetDTO;
import com.studyfi.userandgroup.user.dto.UserDTO;
import com.studyfi.userandgroup.user.exception.EmailNotVerifiedException;
import com.studyfi.userandgroup.user.model.User;
import com.studyfi.userandgroup.user.repo.UserRepo;
import com.studyfi.userandgroup.group.repo.GroupRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.modelmapper.config.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.time.ZoneId;
import java.util.Date;
import java.time.LocalDateTime;
import com.studyfi.userandgroup.user.exception.InvalidVerificationCodeException;
import com.studyfi.userandgroup.user.exception.EmailVerificationCodeInvalidException;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final GroupRepo groupRepo;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    public UserService(UserRepo userRepo, GroupRepo groupRepo, ModelMapper modelMapper, BCryptPasswordEncoder passwordEncoder)
    {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.modelMapper = modelMapper;
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);


        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.reset-password-url}")  // Using an external property for the base URL
    private String resetPasswordUrl;

    @Autowired
    private JavaMailSender mailSender; // autowired JavaMailSender

    // Register a new user
    public UserDTO login(String email, String password) {
        // Find the user by email
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }

        // Check if the password is correct
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Add the email verification
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email address is not verified. Please check your email for the verification code."); // Throw your custom exception
        }

        // Return the UserDTO
        return modelMapper.map(user, UserDTO.class);
    }
    public UserDTO registerUser(UserDTO userDTO) {
        // Check if a user with the same email already exists
        User existingUser = userRepo.findByEmail(userDTO.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("User with this email already exists.");
        }

        // Validate password for registration
        validatePassword(userDTO.getPassword());

        User user = modelMapper.map(userDTO, User.class);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));  // Encrypt password during registration
        userRepo.save(user);

        // Trigger email verification after successful registration
        sendEmailVerificationCode(user.getEmail());

        return modelMapper.map(user, UserDTO.class);
    }

    // Get all users
    public List<UserDTO> getAllUsers() {
        return userRepo.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }

    // Get user by ID
    public UserDTO getUserById(Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(user, UserDTO.class);
    }

    // The method to send reset link email with token and expiration time
    public String sendPasswordResetLink(String email){
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        // Generate a 6-digit verification code
        String verificationCode = generateVerificationCode();
        // Set the expiration time for the code (e.g., 15 minutes from now)
        LocalDateTime verificationCodeExpiryLocalDateTime = LocalDateTime.now().plusMinutes(15);
        Date verificationCodeExpiry = Date.from(verificationCodeExpiryLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // Save the verification code and expiry in the database
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(verificationCodeExpiry);
        userRepo.save(user);
        sendVerificationCode(email,verificationCode);
        return verificationCode;
    }

    private String generateEmailVerificationCode() {
        // Generate a unique code for email verification (can be different logic from password reset)
        // For simplicity, let's use a UUID for now
        return UUID.randomUUID().toString().substring(0, 6); // Example: using first 6 characters of UUID
    }

    public void sendEmailVerificationCode(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Generate a new email verification code
        String verificationCode = generateEmailVerificationCode();

        // Set the expiration time for the code (e.g., 30 minutes from now)
        LocalDateTime verificationCodeExpiry = LocalDateTime.now().plusMinutes(30);

        // Save the email verification code and expiry in the database
        user.setEmailVerificationCode(verificationCode);
        user.setEmailVerificationCodeExpiry(verificationCodeExpiry);
        userRepo.save(user);

        // Send the email with the verification code
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email Verification Code");
        message.setText("Your email verification code is: " + verificationCode + ". This code will expire in 30 minutes.");

        mailSender.send(message);
    }

    public boolean validateEmailVerificationCode(String email, String code) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("User not found with email: " + email);
        }

        // Check if the provided code matches the stored email verification code
        if (user.getEmailVerificationCode() == null || !code.equals(user.getEmailVerificationCode())) {
            throw new InvalidVerificationCodeException("Invalid email verification code");
        }

        // Check if the verification code has expired
        if (user.getEmailVerificationCodeExpiry() == null || LocalDateTime.now().isAfter(user.getEmailVerificationCodeExpiry())) {
            throw new EmailVerificationCodeInvalidException("Email verification code has expired");
        }

        // If the code is valid and not expired, set emailVerified to true and clear the code and expiry
        user.setEmailVerified(true);
        user.setEmailVerificationCode(null);
        user.setEmailVerificationCodeExpiry(null);
        userRepo.save(user);
        return true;
    }

    private void sendVerificationCode(String email, String verificationCode) {
        // Prepare the email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Your verification code is: " + verificationCode + ". This code will expire in 15 minutes.");

        // Send the email
        try {
            mailSender.send(message);
            System.out.println("Verification code sent to: " + email);
        } catch (Exception ex) {
            System.err.println("Error sending email: " + ex.getMessage());
        }
    }

    private String generateVerificationCode() {
        // Generate a random 6-digit number
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    // The method to reset the user's password using the reset token
    public void resetPassword(PasswordResetDTO passwordResetDTO) {
        // Find the user by the reset token
        User user = userRepo.findByEmail(passwordResetDTO.getEmail());
        if (user == null ) {
            throw new RuntimeException("Invalid email address");
        }
        if (!passwordResetDTO.getVerificationCode().equals(user.getVerificationCode())) {
            throw new RuntimeException("Invalid Verification Code");
        }

        // Check if the token has expired
        if (user.getVerificationCodeExpiry().before(new Date())) {
            throw new RuntimeException("Verification code has expired");
        }

        // Validate the new password
        validatePassword(passwordResetDTO.getNewPassword()); // throws exception if invalid password

        // Encrypt the new password before saving
        user.setPassword(passwordEncoder.encode(passwordResetDTO.getNewPassword()));

        // Clear the verification code and expiry after the password reset
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);

        // Save the updated user
        userRepo.save(user);
    }

    // Update user profile
    public UserDTO updateUserProfile(Integer userId, UserDTO userDTO) {
        User user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Check if the updated email is already in use by another user
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            User existingUserWithEmail = userRepo.findByEmail(userDTO.getEmail());
            if (existingUserWithEmail != null) {
                throw new RuntimeException("Email is already in use by another user.");
            }
        }


        // Check if a new profile image URL is provided
        if (userDTO.getProfileImageUrl() != null) {
            user.setProfileImageUrl(userDTO.getProfileImageUrl());
        } else {
            // Keep the old profile image URL
        }

        // Check if a new cover image URL is provided
        if (userDTO.getCoverImageUrl() != null) {
            user.setCoverImageUrl(userDTO.getCoverImageUrl());
        } else {
            // Keep the old cover image URL
        }

        // Update the user fields with new data
        if(userDTO.getEmail() != null){
            user.setEmail(userDTO.getEmail());
        }

        modelMapper.map(userDTO, user);
        // Validate the new password before updating it
        if(userDTO.getPassword() != null){
            validatePassword(userDTO.getPassword());
            // Encrypt the password before saving
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        // Save the updated user
        userRepo.save(user);
        return modelMapper.map(user, UserDTO.class);
    }

    // Add user to a group
    public void addUserToGroup(Integer userId, Integer groupId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

        if (user.getGroups().contains(group)) {
            throw new RuntimeException("User is already in the group.");
        } else {
            user.getGroups().add(group);
            group.getUsers().add(user);

            userRepo.save(user);
            groupRepo.save(group);
        }
    }

    public List<Integer> getUsersByGroupId(Integer groupId) {
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        List<Integer> userIds = new ArrayList<>();

        for (User user : group.getUsers()) {
            userIds.add(user.getId());
        }

        return userIds;
    }


    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";  // Simple regex to validate email format
        return email != null && email.matches(emailRegex);
    }

    // Password validation logic
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Check minimum length (e.g., 8 characters)
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Check if the password contains at least one number
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }

        // Check if the password contains at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        // Check if the password contains at least one special character
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
}
