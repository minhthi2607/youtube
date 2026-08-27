package com.project.youtube.service;

import com.project.youtube.dto.auth.AuthResponse;
import com.project.youtube.dto.auth.LoginRequest;
import com.project.youtube.dto.auth.RegisterRequest;
import com.project.youtube.dto.user.CurrentUserResponse;
import com.project.youtube.entity.User;
import com.project.youtube.exception.ConflictException;
import com.project.youtube.repository.UserRepository;
import com.project.youtube.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return AuthResponse.of(token, toCurrentUserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return AuthResponse.of(token, toCurrentUserResponse(user));
    }

    private CurrentUserResponse toCurrentUserResponse(User user) {
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getDisplayName(), user.getAvatarUrl(), user.getCreatedAt());
    }
}
