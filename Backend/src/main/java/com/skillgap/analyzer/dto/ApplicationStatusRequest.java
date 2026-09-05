package com.skillgap.analyzer.dto;

import com.skillgap.analyzer.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(@NotNull ApplicationStatus status) {}
