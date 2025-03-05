package com.nue.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class RegisterRequest {

    @Schema(description = "Primer nombre del usuario", example = "Erick")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Perez")
    private String lastName;

    @Schema(description = "Correo electrónico del usuario", example = "erick@example.com")
    private String emailAddress;

    @Schema(description = "Contraseña del usuario", example = "password123")
    private String password;

    // Getters y Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
