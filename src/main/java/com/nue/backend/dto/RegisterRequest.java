package com.nue.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class RegisterRequest {

    @Schema(description = "Full name of the user", example = "Erick Perez")
    private String fullname;

    @Schema(description = "User's email address", example = "erick@example.com")
    private String emailAddress;

    @Schema(description = "User's password", example = "password123")
    private String password;

    // Getters y Setters
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
