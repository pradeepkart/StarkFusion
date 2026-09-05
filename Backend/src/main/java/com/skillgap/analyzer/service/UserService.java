package com.skillgap.analyzer.service;

import com.skillgap.analyzer.entity.User;
import com.skillgap.analyzer.exception.ResourceNotFoundException;
import com.skillgap.analyzer.repository.UserRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users = users; }
    public User findByEmail(String email) {
        return users.findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
