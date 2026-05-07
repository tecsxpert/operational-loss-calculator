package com.project.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDTO {
    
    @NotBlank(message = "Username or Email is required")
    private String identifier; // can be username or email
    
    @NotBlank(message = "Password is required")
    private String password;
}
