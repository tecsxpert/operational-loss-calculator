package com.project.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private String token;
    private String username;
    private String email;
    private String role;
}
