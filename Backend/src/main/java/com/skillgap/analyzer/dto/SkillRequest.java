package com.skillgap.analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillRequest(@NotBlank @Size(max = 120) String name, @NotBlank @Size(max = 120) String category) {}
