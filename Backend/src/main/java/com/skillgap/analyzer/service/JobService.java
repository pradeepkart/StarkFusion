package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.entity.*;
import com.skillgap.analyzer.exception.ResourceNotFoundException;
import com.skillgap.analyzer.repository.*;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobs;
    private final SkillRepository skills;
    private final JobSkillRepository jobSkills;
    private final ApplicationRepository applications;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationService recommendations;
    public JobService(JobRepository jobs, SkillRepository skills, JobSkillRepository jobSkills,
                      ApplicationRepository applications, RecommendationRepository recommendationRepository,
                      RecommendationService recommendations) {
        this.jobs = jobs; this.skills = skills; this.jobSkills = jobSkills; this.applications = applications;
        this.recommendationRepository = recommendationRepository; this.recommendations = recommendations;
    }
    public List<JobResponse> all() { return jobs.findAll(Sort.by("jobId")).stream().map(ResponseMapper::job).toList(); }
    public JobResponse get(Long id) { return ResponseMapper.job(find(id)); }
    public List<JobSkillResponse> skills(Long id) {
        find(id);
        return jobSkills.findByJobJobIdOrderBySkillSkillId(id).stream().map(ResponseMapper::jobSkill).toList();
    }
    @Transactional
    public JobResponse create(JobRequest request) {
        Job job = new Job();
        updateFields(job, request);
        return ResponseMapper.job(jobs.saveAndFlush(job));
    }
    @Transactional
    public JobResponse update(Long id, JobRequest request) {
        Job job = find(id);
        updateFields(job, request);
        return ResponseMapper.job(job);
    }
    @Transactional
    public void delete(Long id) {
        Job job = find(id);
        if (applications.existsByJobJobId(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Job has applications and cannot be deleted");
        recommendationRepository.deleteByJobJobId(id);
        jobSkills.deleteByJobJobId(id);
        recommendationRepository.flush();
        jobSkills.flush();
        jobs.delete(job);
        jobs.flush();
    }
    @Transactional
    public JobSkillResponse upsertSkill(Long id, JobSkillRequest request) {
        Job job = find(id);
        Skill skill = skills.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id " + request.skillId()));
        JobSkill assignment = jobSkills.findByJobJobIdAndSkillSkillId(id, request.skillId()).orElseGet(JobSkill::new);
        assignment.setJob(job);
        assignment.setSkill(skill);
        assignment.setRequiredLevel(request.requiredLevel());
        assignment.setMandatory(request.mandatory());
        jobSkills.saveAndFlush(assignment);
        recommendations.refreshJob(job);
        return ResponseMapper.jobSkill(assignment);
    }
    @Transactional
    public void deleteSkill(Long id, Long skillId) {
        Job job = find(id);
        JobSkill assignment = jobSkills.findByJobJobIdAndSkillSkillId(id, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Job skill not found with skill id " + skillId));
        jobSkills.delete(assignment);
        jobSkills.flush();
        recommendations.refreshJob(job);
    }
    private Job find(Long id) { return jobs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id)); }
    private void updateFields(Job job, JobRequest request) {
        job.setCompany(request.company().strip()); job.setTitle(request.title().strip()); job.setLocation(request.location().strip());
    }
}
