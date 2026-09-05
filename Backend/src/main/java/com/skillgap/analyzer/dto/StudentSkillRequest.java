package com.skillgap.analyzer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StudentSkillRequest(@NotNull @Positive Long skillId, @NotNull @Min(1) @Max(5) Integer proficiency) {}
