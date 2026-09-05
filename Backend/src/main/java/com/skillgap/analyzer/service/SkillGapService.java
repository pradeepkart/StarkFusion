package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.entity.*;
import com.skillgap.analyzer.exception.ResourceNotFoundException;
import com.skillgap.analyzer.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SkillGapService {
    private final StudentRepository students;
    private final JobRepository jobs;
    private final StudentSkillRepository studentSkills;
    private final JobSkillRepository jobSkills;
    public SkillGapService(StudentRepository students, JobRepository jobs,
                           StudentSkillRepository studentSkills, JobSkillRepository jobSkills) {
        this.students = students; this.jobs = jobs; this.studentSkills = studentSkills; this.jobSkills = jobSkills;
    }

    public SkillGapResult analyze(Long studentId, Long jobId) {
        if (!students.existsById(studentId)) throw new ResourceNotFoundException("Student not found with id " + studentId);
        Job job = jobs.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));
        Map<Long, Integer> levels = studentSkills.findByStudentStudentIdOrderBySkillSkillId(studentId).stream()
                .collect(Collectors.toMap(s -> s.getSkill().getSkillId(), StudentSkill::getProficiency));
        List<JobSkillResponse> requirements = jobSkills.findByJobJobIdOrderBySkillSkillId(jobId).stream()
                .map(ResponseMapper::jobSkill).toList();
        return calculate(ResponseMapper.job(job), levels, requirements);
    }

    // Pure calculation; usable independently of the database in unit tests.
    public SkillGapResult calculate(JobResponse job, Map<Long, Integer> levels, List<JobSkillResponse> requirements) {
        List<SkillGapDetail> details = new ArrayList<>();
        double weightedMatch = 0;
        int totalWeight = 0;
        boolean mandatorySkillsMet = true;
        for (JobSkillResponse requirement : requirements) {
            int current = levels.getOrDefault(requirement.skillId(), 0);
            int gap = Math.max(requirement.requiredLevel() - current, 0);
            double skillMatch = Math.min((double) current / requirement.requiredLevel(), 1.0) * 100;
            int weight = requirement.mandatory() ? 2 : 1;
            weightedMatch += skillMatch * weight;
            totalWeight += weight;
            if (requirement.mandatory() && gap > 0) mandatorySkillsMet = false;
            details.add(new SkillGapDetail(requirement.skillId(), requirement.skillName(), current,
                    requirement.requiredLevel(), gap, requirement.mandatory(), round(skillMatch), gap == 0 ? "MATCHED" : "GAP"));
        }
        boolean evaluable = totalWeight > 0;
        return new SkillGapResult(job.jobId(), job.title(), job.company(),
                evaluable ? round(weightedMatch / totalWeight) : BigDecimal.ZERO.setScale(2),
                evaluable, evaluable && mandatorySkillsMet, List.copyOf(details));
    }

    private BigDecimal round(double value) { return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP); }
}
