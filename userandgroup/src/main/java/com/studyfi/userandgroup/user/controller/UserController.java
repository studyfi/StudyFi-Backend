package com.studyfi.userandgroup.user.controller;

import com.studyfi.userandgroup.service.CloudinaryService;
import com.studyfi.userandgroup.user.dto.EmailRequestDTO;
import com.studyfi.userandgroup.user.dto.PasswordResetResponseDTO;
import com.studyfi.userandgroup.user.dto.PasswordResetDTO;
import com.studyfi.userandgroup.user.dto.UserDTO;
import com.studyfi.userandgroup.user.exception.EmailNotVerifiedException;
import com.studyfi.userandgroup.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Register a new user
    @PostMapping(value= "/register" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Changed mapping to /register
    public UserDTO registerUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneContact") String phoneContact,
            @RequestParam("birthDate") String birthDate,
            @RequestParam("country") String country,
            @RequestParam("aboutMe") String aboutMe,
            @RequestParam("currentAddress") String currentAddress,
            @RequestParam(value = "profileFile", required = false) MultipartFile profileFile,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile) throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(name);
        userDTO.setEmail(email);
        userDTO.setPassword(password);
        userDTO.setPhoneContact(phoneContact);
        userDTO.setBirthDate(birthDate);
        userDTO.setCountry(country);
        userDTO.setAboutMe(aboutMe);
        userDTO.setCurrentAddress(currentAddress);
        if (profileFile != null) {
            userDTO.setProfileImageUrl(cloudinaryService.uploadFile(profileFile));
        }
        if (coverFile != null) {
            userDTO.setCoverImageUrl(cloudinaryService.uploadFile(coverFile));
        }

        return userService.registerUser(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("email") String email, @RequestParam("password") String password) { // Change return type to ResponseEntity<?>
        try {
            UserDTO userDTO = userService.login(email, password);
            return ResponseEntity.ok(userDTO); // Return 200 OK with UserDTO if login is successful
        } catch (EmailNotVerifiedException e) {
            // Handle email not verified exception
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // Return 401 Unauthorized
        } catch (RuntimeException e) {
            // Handle other login errors (e.g., invalid credentials)
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // Return 401 Unauthorized for invalid credentials
        } catch (Exception e) {
            // Handle any other unexpected errors
            Map<String, String> response = new HashMap<>();
            response.put("message", "An error occurred during login.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Return 500 Internal Server Error
        }
    }

    // Get all users
    @GetMapping("/getusers")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get User by ID
    @GetMapping("/{userId}")
    public UserDTO getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    // Endpoint to trigger sending the password reset link
    @PostMapping("/forgot-password") // Update this method
    public PasswordResetResponseDTO sendPasswordResetEmail(@RequestBody EmailRequestDTO emailRequestDTO) {
        String verificationCode = userService.sendPasswordResetLink(emailRequestDTO.getEmail()); // Calls the service method

        PasswordResetResponseDTO response = new PasswordResetResponseDTO();
        response.setMessage("Password reset link sent to " + emailRequestDTO.getEmail());
        response.setVerificationCode(verificationCode);

        return response;
    }

    // Endpoint to reset password
    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody PasswordResetDTO passwordResetDTO) {
        userService.resetPassword(passwordResetDTO);
        return "Password has been successfully reset for: "+ passwordResetDTO.getEmail();
    }

    //Update user profile

    @PutMapping(value="/profile/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDTO updateProfile(
            @PathVariable Integer userId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value="password", required = false) String password,
            @RequestParam("phoneContact") String phoneContact,
            @RequestParam("birthDate") String birthDate,
            @RequestParam("country") String country,
            @RequestParam("aboutMe") String aboutMe,
            @RequestParam("currentAddress") String currentAddress,
            @RequestParam(value = "profileFile", required = false) MultipartFile profileFile,
            @RequestParam(value = "oldProfileUrl", required = false) String oldProfileUrl,
            @RequestParam(value = "oldCoverUrl", required = false) String oldCoverUrl,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile) throws Exception{

        UserDTO userDTO = new UserDTO();
        userDTO.setName(name);
        userDTO.setEmail(email);
        userDTO.setPassword(password);
        userDTO.setPhoneContact(phoneContact);
        userDTO.setBirthDate(birthDate);
        userDTO.setCountry(country);
        userDTO.setAboutMe(aboutMe);
        userDTO.setCurrentAddress(currentAddress);
        if (profileFile != null){
            userDTO.setProfileImageUrl(cloudinaryService.uploadFile(profileFile));
        } else{
            userDTO.setProfileImageUrl(oldProfileUrl);
        }
        if(coverFile != null){
            userDTO.setCoverImageUrl(cloudinaryService.uploadFile(coverFile));
        }else{
            userDTO.setCoverImageUrl(oldCoverUrl);
        }


        return userService.updateUserProfile(userId,userDTO);
    }

    // Add user to group
    @PostMapping("/addToGroup")
    public void addUserToGroup(@RequestParam Integer userId, @RequestParam Integer groupId) {  // Changed Long to Integer
        userService.addUserToGroup(userId, groupId);
    }
}