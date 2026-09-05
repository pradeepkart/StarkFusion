package com.skillgap.analyzer.dto;

import java.math.BigDecimal;

public record DashboardResponse(long totalStudents, long totalJobs, long totalApplications, BigDecimal averageSkillMatch) {}
