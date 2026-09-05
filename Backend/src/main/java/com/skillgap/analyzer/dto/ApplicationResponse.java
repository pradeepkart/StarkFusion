package com.skillgap.analyzer.dto;

import com.skillgap.analyzer.entity.ApplicationStatus;
import java.math.BigDecimal;

public record ApplicationResponse(Long id, Long studentId, String studentName, Long jobId, String jobTitle, String company, BigDecimal matchPercent, ApplicationStatus status) {}
