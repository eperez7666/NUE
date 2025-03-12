package com.nue.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nue.backend.dto.ApiResponse;
import com.nue.backend.dto.ResetPasswordRequest;
import com.nue.backend.email.EmailService;
import com.nue.backend.model.Role;
import com.nue.backend.model.User;
import com.nue.backend.repository.UserRepository;
import com.nue.backend.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ✅ Reusable method for error responses
    private ResponseEntity<ApiResponse> buildErrorResponse(int status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse(status, message, null));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a user", description = "Allows registering a user with basic information.")
    public ResponseEntity<ApiResponse> register(
            @RequestParam(name = "fullname") String fullname,
            @RequestParam(name = "emailAddress") String emailAddress,
            @RequestParam(name = "password") String password) {

        if (userRepository.findByEmailAddress(emailAddress).isPresent()) {
            return buildErrorResponse(400, "The provided email address is already registered.");
        }

        User newUser = new User(fullname, emailAddress, passwordEncoder.encode(password), Role.USER);
        userRepository.save(newUser);

        return ResponseEntity.ok(new ApiResponse(200, "User successfully registered.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @RequestParam(name = "emailAddress") String emailAddress,
            @RequestParam(name = "password") String password) {

        Optional<User> userOptional = userRepository.findByEmailAddress(emailAddress);
        if (userOptional.isEmpty()) {
            return buildErrorResponse(400, "No user found with the provided email address.");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(emailAddress, password));
        } catch (BadCredentialsException e) {
            return buildErrorResponse(403, "Incorrect password. Please try again.");
        }

        User user = userOptional.get();
        return ResponseEntity.ok(new ApiResponse(200, "Login successful.",
                Map.of("username", user.getFullname())));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam(name = "emailAddress") String emailAddress) {

        Optional<User> userOptional = userRepository.findByEmailAddress(emailAddress);
        if (userOptional.isEmpty()) {
            return buildErrorResponse(400, "The provided email address is not registered.");
        }

        User user = userOptional.get();
        String resetToken = jwtUtil.generateResetToken(user.getEmailAddress());
        long expiresIn = 30 * 60;

        user.setResetToken(resetToken);
        userRepository.save(user);

        emailService.sendPasswordResetNotification(user.getEmailAddress(), user.getFullname());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("username", user.getFullname());
        responseData.put("token", resetToken);
        responseData.put("expiresIn", expiresIn);

        return ResponseEntity.ok(new ApiResponse(200, "Password reset token successfully generated.", responseData));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Allows changing the password using a recovery token.")
    public ResponseEntity<ApiResponse> resetPassword(@org.springframework.web.bind.annotation.RequestBody ResetPasswordRequest request) {

        // 🔍 Debugging: Print received values
        System.out.println("Received in reset-password: " + request);
        System.out.println("Token: " + request.getToken());
        System.out.println("New Password: " + request.getNewPassword());

        if (request == null || request.getToken() == null || request.getNewPassword() == null) {
            return buildErrorResponse(400, "Invalid request. Both 'token' and 'newPassword' fields are required.");
        }

        String token = request.getToken();
        String newPassword = request.getNewPassword();

        try {
            // Extract email from the token
            String emailAddress = jwtUtil.extractUsername(token);
            Optional<User> userOptional = userRepository.findByEmailAddress(emailAddress);

            if (userOptional.isEmpty()) {
                return buildErrorResponse(400, "Invalid or expired token. No user found.");
            }

            User user = userOptional.get();
            String fullName = user.getFullname(); // Correctly extracting full name

            // Check if the token has expired
            if (jwtUtil.isTokenExpired(token)) {
                return ResponseEntity.status(400).body(new ApiResponse(400, "The token has expired. Please request a new password reset.",
                        Map.of("email", emailAddress, "fullName", fullName)));
            }

            // Validate the token
            if (!jwtUtil.validateToken(token, new org.springframework.security.core.userdetails.User(
                    user.getEmailAddress(), user.getPassword(), new ArrayList<>()))) {
                return buildErrorResponse(400, "Invalid or expired token. Please request a new reset token.");
            }

            // Update the password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            userRepository.save(user);

            // Send confirmation email
            emailService.sendPasswordChangedEmail(user.getEmailAddress(), user.getFullname());

            return ResponseEntity.ok(new ApiResponse(200, "Password successfully updated.",
                    Map.of("email", emailAddress, "fullName", fullName)));

        } catch (Exception e) {
            return buildErrorResponse(400, "An error occurred while processing the request: " + e.getMessage());
        }
    }
}
