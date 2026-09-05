package com.skillgap.analyzer.dto;



public record JobSkillResponse(Long id, Long skillId, String skillName, int requiredLevel, boolean mandatory) {}
