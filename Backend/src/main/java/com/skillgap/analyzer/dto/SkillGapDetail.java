package com.skillgap.analyzer.dto;

import java.math.BigDecimal;

public record SkillGapDetail(Long skillId, String skillName, int currentLevel, int requiredLevel, int gap, boolean mandatory, BigDecimal matchPercent, String status) {}
