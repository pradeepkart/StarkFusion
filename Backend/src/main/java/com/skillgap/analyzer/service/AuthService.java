package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.entity.*;
import com.skillgap.analyzer.repository.*;
import com.skillgap.analyzer.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final StudentRepository students;
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwt;
    private final RecommendationService recommendations;
    public AuthService(UserRepository users, StudentRepository students, UserService userService,
                       BCryptPasswordEncoder encoder, AuthenticationManager authenticationManager,
                       JwtService jwt, RecommendationService recommendations) {
        this.users = users; this.students = students; this.userService = userService; this.encoder = encoder;
        this.authenticationManager = authenticationManager; this.jwt = jwt; this.recommendations = recommendations;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes for BCrypt");
        User user = new User();
        user.setName(request.name().strip());
        user.setEmail(email);
        user.setPassword(encoder.encode(request.password()));
        user.setRole(Role.ROLE_USER); // Registration can never choose or elevate a role.
        users.saveAndFlush(user);
        Student student = new Student();
        student.setName(user.getName());
        student.setEmail(user.getEmail());
        student.setUser(user);
        students.saveAndFlush(student);
        recommendations.refreshStudent(student);
        return new MessageResponse("User registered successfully");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) throw new BadCredentialsException("Invalid credentials");
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userService.findByEmail(email);
        return new AuthResponse(jwt.generateToken((UserDetails) authentication.getPrincipal()), "Bearer",
                user.getName(), user.getEmail(), user.getRole());
    }
}
