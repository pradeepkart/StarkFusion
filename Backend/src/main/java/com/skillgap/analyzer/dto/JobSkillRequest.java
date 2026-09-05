package com.skillgap.analyzer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record JobSkillRequest(@NotNull @Positive Long skillId, @NotNull @Min(1) @Max(5) Integer requiredLevel, @NotNull Boolean mandatory) {}
