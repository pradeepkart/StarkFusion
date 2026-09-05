package com.skillgap.analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobRequest(@NotBlank @Size(max = 160) String company, @NotBlank @Size(max = 160) String title, @NotBlank @Size(max = 160) String location) {}
