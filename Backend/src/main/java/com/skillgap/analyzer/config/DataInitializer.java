package com.skillgap.analyzer.config;

import com.skillgap.analyzer.entity.*;
import com.skillgap.analyzer.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;
    private final boolean enabled;
    private final String name;
    private final String email;
    private final String password;
    public DataInitializer(UserRepository users, BCryptPasswordEncoder encoder,
                           @Value("${app.admin.enabled:true}") boolean enabled,
                           @Value("${app.admin.name}") String name, @Value("${app.admin.email}") String email,
                           @Value("${app.admin.password}") String password) {
        this.users = users; this.encoder = encoder; this.enabled = enabled; this.name = name;
        this.email = email.strip().toLowerCase(Locale.ROOT); this.password = password;
    }
    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) return;
        var existing = users.findByEmail(email);
        if (existing.isPresent()) {
            if (existing.get().getRole() != Role.ROLE_ADMIN)
                throw new IllegalStateException("Configured admin email belongs to a non-admin; refusing to elevate its role");
            return;
        }
        if (password.length() < 6 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalStateException("Admin password must have at least 6 characters and at most 72 UTF-8 bytes");
        User admin = new User();
        admin.setName(name); admin.setEmail(email); admin.setPassword(encoder.encode(password)); admin.setRole(Role.ROLE_ADMIN);
        users.save(admin);
    }
}
