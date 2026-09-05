package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.entity.*;
import com.skillgap.analyzer.repository.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecommendationService {
    private final RecommendationRepository recommendations;
    private final StudentRepository students;
    private final JobRepository jobs;
    private final SkillRepository skills;
    private final SkillGapService skillGaps;
    public RecommendationService(RecommendationRepository recommendations, StudentRepository students,
                                 JobRepository jobs, SkillRepository skills, SkillGapService skillGaps) {
        this.recommendations = recommendations; this.students = students;
        this.jobs = jobs; this.skills = skills; this.skillGaps = skillGaps;
    }

    public List<RecommendationResponse> getRecommendations(Long studentId, Long jobId) {
        return generate(skillGaps.analyze(studentId, jobId));
    }

    public List<RecommendationResponse> generate(SkillGapResult result) {
        return result.skills().stream().filter(s -> s.gap() > 0).map(s -> {
            int priority = s.mandatory() ? (s.gap() >= 2 ? 1 : 2) : 3;
            String reason = s.skillName() + " is " + (s.mandatory() ? "mandatory" : "optional")
                    + ". Current level is " + s.currentLevel() + " and required level is " + s.requiredLevel() + ".";
            return new RecommendationResponse(s.skillId(), s.skillName(), s.currentLevel(), s.requiredLevel(),
                    s.gap(), priority, reason);
        }).sorted(Comparator.comparingInt(RecommendationResponse::priority)
                .thenComparing(Comparator.comparingInt(RecommendationResponse::gap).reversed())
                .thenComparing(RecommendationResponse::skillId)).toList();
    }

    @Transactional
    public void refreshStudent(Student student) {
        for (Job job : jobs.findAll()) refresh(student, job);
    }

    @Transactional
    public void refreshJob(Job job) {
        for (Student student : students.findAll()) refresh(student, job);
    }

    @Transactional
    public void refreshAll() {
        for (Student student : students.findAll()) refreshStudent(student);
    }

    private void refresh(Student student, Job job) {
        Map<Long, Recommendation> existing = recommendations
                .findByStudentStudentIdAndJobJobId(student.getStudentId(), job.getJobId()).stream()
                .collect(Collectors.toMap(r -> r.getSkill().getSkillId(), Function.identity()));
        for (RecommendationResponse item : getRecommendations(student.getStudentId(), job.getJobId())) {
            Recommendation recommendation = existing.remove(item.skillId());
            if (recommendation == null) {
                recommendation = new Recommendation();
                recommendation.setStudent(student);
                recommendation.setJob(job);
                recommendation.setSkill(skills.getReferenceById(item.skillId()));
            }
            recommendation.setPriority(item.priority());
            recommendation.setReason(item.reason());
            recommendations.save(recommendation);
        }
        recommendations.deleteAll(existing.values());
    }
}
