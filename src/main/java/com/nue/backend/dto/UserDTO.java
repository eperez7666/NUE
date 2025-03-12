package com.nue.backend.dto;

import com.nue.backend.model.Role;

public class UserDTO {
    private Long id;
    private String fullname;  // 🔹 Cambio a fullname
    private String emailAddress;
    private Role role;

    public UserDTO(Long id, String fullname, String emailAddress, Role role) {
        this.id = id;
        this.fullname = fullname;
        this.emailAddress = emailAddress;
        this.role = role;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullname() { return fullname; }  // 🔹 Nuevo getter y setter
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
