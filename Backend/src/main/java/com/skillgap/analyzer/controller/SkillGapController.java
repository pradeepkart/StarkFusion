package com.skillgap.analyzer.controller;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.service.*;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/jobs/{jobId}")
@PreAuthorize("hasRole('USER')")
public class SkillGapController {
    private final StudentService students;
    private final SkillGapService skillGaps;
    private final RecommendationService recommendations;
    public SkillGapController(StudentService students, SkillGapService skillGaps, RecommendationService recommendations) {
        this.students = students; this.skillGaps = skillGaps; this.recommendations = recommendations;
    }
    @GetMapping("/skill-gap")
    public SkillGapResult analyze(Principal principal, @PathVariable @Positive Long jobId) {
        return skillGaps.analyze(students.currentStudent(principal.getName()).getStudentId(), jobId);
    }
    @GetMapping("/recommendations")
    public List<RecommendationResponse> recommendations(Principal principal, @PathVariable @Positive Long jobId) {
        return recommendations.getRecommendations(students.currentStudent(principal.getName()).getStudentId(), jobId);
    }
}
