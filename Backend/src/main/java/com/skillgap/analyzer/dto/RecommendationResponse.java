package com.skillgap.analyzer.dto;



public record RecommendationResponse(Long skillId, String skillName, int currentLevel, int requiredLevel, int gap, int priority, String reason) {}
