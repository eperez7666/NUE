package com.nue.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    // 📌 Método reutilizable para respuestas de error
    private ResponseEntity<ApiResponse> buildErrorResponse(int status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse(status, message, null));
    }

    // ✅ ENDPOINT: REGISTRAR USUARIO
    @PostMapping("/register")
    @Operation(summary = "Registrar un usuario", description = "Permite registrar un usuario con sus datos básicos.")
    public ResponseEntity<ApiResponse> register(
            @RequestParam(name = "firstName") String firstName,
            @RequestParam(name = "lastName") String lastName,
            @RequestParam(name = "emailAddress") String emailAddress,
            @RequestParam(name = "password") String password) {

        if (userRepository.findByEmailAddress(emailAddress).isPresent()) {
            return buildErrorResponse(400, "Email ya registrado");
        }

        User newUser = new User(firstName, lastName, emailAddress, passwordEncoder.encode(password), Role.USER);
        userRepository.save(newUser);

        return ResponseEntity.ok(new ApiResponse(200, "Usuario registrado exitosamente", null));
    }

    // ✅ ENDPOINT: INICIO DE SESIÓN (LOGIN)
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un mensaje de éxito.")
    public ResponseEntity<ApiResponse> login(
            @RequestParam(name = "emailAddress") String emailAddress,
            @RequestParam(name = "password") String password) {

        Optional<User> userOptional = userRepository.findByEmailAddress(emailAddress);
        if (userOptional.isEmpty()) {
            return buildErrorResponse(400, "Usuario no encontrado");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(emailAddress, password)
        );

        // Se genera el token pero NO se expone en la respuesta
        jwtUtil.generateToken(new UsernamePasswordAuthenticationToken(emailAddress, password));

        return ResponseEntity.ok(new ApiResponse(200, "Inicio de sesión exitoso", null));
    }

    // ✅ ENDPOINT: SOLICITAR RECUPERACIÓN DE CONTRASEÑA
    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un correo con instrucciones para restablecer la contraseña.")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam(name = "emailAddress") String emailAddress) {

        Optional<User> userOptional = userRepository.findByEmailAddress(emailAddress);
        if (userOptional.isEmpty()) {
            return buildErrorResponse(400, "Correo no registrado");
        }

        User user = userOptional.get();

        // Generar un token de recuperación de contraseña
        String resetToken = jwtUtil.generateResetToken(user.getEmailAddress());

        // Guardar el token en la BD sin exponerlo en la URL
        user.setResetToken(resetToken);
        userRepository.save(user);

        // Enviar email con un enlace seguro
        String resetLink = "http://localhost:3000/reset-password";
        emailService.sendPasswordResetInstructions(user.getEmailAddress(), resetLink);

        return ResponseEntity.ok(new ApiResponse(200, "Correo de recuperación enviado", null));
    }

    // ✅ ENDPOINT: RESTABLECER CONTRASEÑA
    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Permite cambiar la contraseña con un token de recuperación.")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {

        String token = request.getToken();
        String newPassword = request.getNewPassword();

        if (token == null || newPassword == null) {
            return buildErrorResponse(400, "Datos incompletos");
        }

        // Extraer email desde el token
        String emailAddress = jwtUtil.extractUsername(token);
        Optional<User> userOptional = userRepository.findByEmailAddress(emailAddress);
        
        if (userOptional.isEmpty()) {
            return buildErrorResponse(400, "Token inválido o expirado");
        }

        User user = userOptional.get();

        if (!jwtUtil.validateToken(token, new org.springframework.security.core.userdetails.User(
                user.getEmailAddress(), user.getPassword(), List.of()))) {
            return buildErrorResponse(400, "Token inválido o expirado");
        }

        // Actualizar la contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);

        // Enviar correo de confirmación
        emailService.sendPasswordChangedEmail(user.getEmailAddress());

        return ResponseEntity.ok(new ApiResponse(200, "Contraseña actualizada correctamente", null));
    }
}
