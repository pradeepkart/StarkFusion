package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.entity.*;
import com.skillgap.analyzer.exception.ResourceNotFoundException;
import com.skillgap.analyzer.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ApplicationService {
    private final ApplicationRepository applications;
    private final JobRepository jobs;
    private final StudentService students;
    private final SkillGapService skillGaps;
    public ApplicationService(ApplicationRepository applications, JobRepository jobs,
                              StudentService students, SkillGapService skillGaps) {
        this.applications = applications; this.jobs = jobs; this.students = students; this.skillGaps = skillGaps;
    }
    @Transactional
    public ApplicationResponse apply(String email, ApplicationRequest request) {
        Student student = students.currentStudent(email);
        Job job = jobs.findById(request.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + request.jobId()));
        if (applications.existsByStudentStudentIdAndJobJobId(student.getStudentId(), job.getJobId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied for this job");
        SkillGapResult result = skillGaps.analyze(student.getStudentId(), job.getJobId());
        Application application = new Application();
        application.setStudent(student);
        application.setJob(job);
        application.setMatchPercent(result.overallMatchPercent());
        application.setStatus(ApplicationStatus.APPLIED);
        return ResponseMapper.application(applications.saveAndFlush(application));
    }
    public List<ApplicationResponse> current(String email) {
        return applications.findByStudentStudentIdOrderByIdDesc(students.currentStudent(email).getStudentId())
                .stream().map(ResponseMapper::application).toList();
    }
    public List<ApplicationResponse> all() {
        return applications.findAllByOrderByIdDesc().stream().map(ResponseMapper::application).toList();
    }
    @Transactional
    public ApplicationResponse updateStatus(Long id, ApplicationStatusRequest request) {
        Application application = applications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));
        application.setStatus(request.status());
        return ResponseMapper.application(application);
    }
}
