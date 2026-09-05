package com.skillgap.analyzer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank @Size(max = 100) String name, @NotBlank @Email @Size(max = 100) String email, @NotBlank @Size(min = 6, max = 72) String password) {
    @Override public String toString() { return "RegisterRequest[credentials redacted]"; }
}
