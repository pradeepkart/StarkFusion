package com.skillgap.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ApplicationRequest(@NotNull @Positive Long jobId) {}
