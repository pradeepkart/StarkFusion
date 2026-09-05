package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.DashboardResponse;
import com.skillgap.analyzer.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final StudentRepository students;
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    public DashboardService(StudentRepository students, JobRepository jobs, ApplicationRepository applications) {
        this.students = students; this.jobs = jobs; this.applications = applications;
    }
    public DashboardResponse getDashboard() {
        Double average = applications.averageMatchPercent();
        return new DashboardResponse(students.count(), jobs.count(), applications.count(),
                BigDecimal.valueOf(average == null ? 0 : average).setScale(2, RoundingMode.HALF_UP));
    }
}
