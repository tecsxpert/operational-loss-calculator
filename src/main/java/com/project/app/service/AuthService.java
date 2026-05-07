package com.project.app.service;

import com.project.app.config.JwtUtil;
import com.project.app.dto.AuthRequestDTO;
import com.project.app.dto.AuthResponseDTO;
import com.project.app.dto.RegisterRequestDTO;
import com.project.app.entity.User;
import com.project.app.exception.CustomException;
import com.project.app.repository.UserRepository;
import com.project.app.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new CustomException("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new CustomException("Email is already in use!", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Constants.ROLE_USER)
                .build();

        userRepository.save(user);

        return authenticateAndGenerateToken(user.getUsername(), registerRequest.getPassword(), user.getEmail(), user.getRole());
    }

    public AuthResponseDTO login(AuthRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getIdentifier())
                .orElseGet(() -> userRepository.findByEmail(loginRequest.getIdentifier())
                        .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND)));

        return authenticateAndGenerateToken(user.getUsername(), loginRequest.getPassword(), user.getEmail(), user.getRole());
    }

    private AuthResponseDTO authenticateAndGenerateToken(String username, String password, String email, String role) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);

        return AuthResponseDTO.builder()
                .token(jwt)
                .username(username)
                .email(email)
                .role(role)
                .build();
    }
}
