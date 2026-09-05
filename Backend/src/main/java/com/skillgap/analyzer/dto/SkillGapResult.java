package com.skillgap.analyzer.dto;

import java.math.BigDecimal;
import java.util.List;

public record SkillGapResult(Long jobId, String jobTitle, String company, BigDecimal overallMatchPercent, boolean evaluable, boolean mandatorySkillsMet, List<SkillGapDetail> skills) {}
