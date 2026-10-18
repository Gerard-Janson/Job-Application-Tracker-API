package com.gerard.jobapplicationtracker.controller;

import com.gerard.jobapplicationtracker.dtos.AuthResponse;
import com.gerard.jobapplicationtracker.dtos.LoginRequest;
import com.gerard.jobapplicationtracker.dtos.RegisterRequest;
import com.gerard.jobapplicationtracker.entity.User;
import com.gerard.jobapplicationtracker.repository.UserRepository;
import com.gerard.jobapplicationtracker.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/tracker/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateJwtToken(userDetails.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwtUtil.getJwtExpirationMs() / 1000));
    }
}
