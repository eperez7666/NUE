package com.nue.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ResetPasswordRequest {

    @Schema(description = "Token de recuperación de contraseña enviado al usuario", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Nueva contraseña", example = "NuevaPassword123")
    private String newPassword;

    // 🔹 Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "token='" + token + '\'' +
                ", newPassword='" + newPassword + '\'' +
                '}';
    }
}
