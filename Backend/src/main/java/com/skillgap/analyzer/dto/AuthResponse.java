package com.skillgap.analyzer.dto;

import com.skillgap.analyzer.entity.Role;

public record AuthResponse(String token, String type, String name, String email, Role role) {
    @Override public String toString() { return "AuthResponse[token redacted, role=" + role + "]"; }
}
