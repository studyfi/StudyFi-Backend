package com.studyfi.userandgroup.user.service;

import com.studyfi.userandgroup.group.model.Group;
import com.studyfi.userandgroup.user.dto.PasswordResetDTO;
import com.studyfi.userandgroup.user.dto.UserDTO;
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

import java.util.Date;
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

        // Return the UserDTO
        return modelMapper.map(user, UserDTO.class);
    }
    public UserDTO registerUser(UserDTO userDTO) {
        // Validate password for registration
        validatePassword(userDTO.getPassword());

        User user = modelMapper.map(userDTO, User.class);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));  // Encrypt password during registration
        userRepo.save(user);
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
    public void sendPasswordResetLink(String email) {
        // Generate a random token for password reset
        String resetToken = UUID.randomUUID().toString();

        // Set the expiration time for the token (e.g., 1 hour from now)
        Date expiryDate = new Date(System.currentTimeMillis() + 3600 * 1000);  // 1 hour expiry time

        // Save this reset token and expiration time in the database for the user
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(expiryDate);  // Save the expiration time
        userRepo.save(user);  // Save the user with the reset token and expiry time

        // Create the complete URL for password reset with the real domain
        String resetLink = resetPasswordUrl + "?token=" + resetToken;

        // Prepare the email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Click the following link to reset your password: " + resetLink);

        // Send the email
        try {
            mailSender.send(message);
            System.out.println("Password reset email sent to: " + email);
        } catch (Exception ex) {
            System.err.println("Error sending email: " + ex.getMessage());
        }
    }

    // The method to reset the user's password using the reset token
    public void resetPassword(String token, PasswordResetDTO passwordResetDTO) {
        // Find the user by the reset token
        User user = userRepo.findByResetToken(token);
        if (user == null) {
            throw new RuntimeException("Invalid reset token");
        }

        // Check if the token has expired
        if (user.getResetTokenExpiry().before(new Date())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Validate the new password
        validatePassword(passwordResetDTO.getNewPassword());

        // Encrypt the new password before saving
        user.setPassword(passwordEncoder.encode(passwordResetDTO.getNewPassword()));  // BCrypt encoding

        // Clear the reset token and expiry after the password reset
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        // Save the updated user
        userRepo.save(user);
    }

    // Update user profile
    public UserDTO updateUserProfile(Integer userId, UserDTO userDTO) {
        User user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

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

        user.getGroups().add(group);
        group.getUsers().add(user);

        userRepo.save(user);
        groupRepo.save(group);
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
