package com.skillgap.analyzer.controller;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.service.JobService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobController {
    private final JobService jobs;
    public JobController(JobService jobs) { this.jobs = jobs; }
    @GetMapping({"/api/jobs", "/api/admin/jobs", "/api/user/jobs"})
    public List<JobResponse> all() { return jobs.all(); }
    @GetMapping({"/api/jobs/{jobId}", "/api/admin/jobs/{jobId}", "/api/user/jobs/{jobId}"})
    public JobResponse get(@PathVariable @Positive Long jobId) { return jobs.get(jobId); }
    @GetMapping({"/api/jobs/{jobId}/skills", "/api/admin/jobs/{jobId}/skills"})
    public List<JobSkillResponse> skills(@PathVariable @Positive Long jobId) { return jobs.skills(jobId); }
    @PostMapping("/api/admin/jobs") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
    public JobResponse create(@Valid @RequestBody JobRequest request) { return jobs.create(request); }
    @PutMapping("/api/admin/jobs/{jobId}") @PreAuthorize("hasRole('ADMIN')")
    public JobResponse update(@PathVariable @Positive Long jobId, @Valid @RequestBody JobRequest request) {
        return jobs.update(jobId, request);
    }
    @DeleteMapping("/api/admin/jobs/{jobId}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable @Positive Long jobId) { jobs.delete(jobId); }
    @PostMapping("/api/admin/jobs/{jobId}/skills") @PreAuthorize("hasRole('ADMIN')")
    public JobSkillResponse upsertSkill(@PathVariable @Positive Long jobId, @Valid @RequestBody JobSkillRequest request) {
        return jobs.upsertSkill(jobId, request);
    }
    @DeleteMapping("/api/admin/jobs/{jobId}/skills/{skillId}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')")
    public void deleteSkill(@PathVariable @Positive Long jobId, @PathVariable @Positive Long skillId) { jobs.deleteSkill(jobId, skillId); }
}
