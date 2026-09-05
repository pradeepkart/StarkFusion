package com.skillgap.analyzer.controller;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.service.*;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final DashboardService dashboard;
    private final StudentService students;
    public AdminController(DashboardService dashboard, StudentService students) { this.dashboard = dashboard; this.students = students; }
    @GetMapping("/dashboard")
    public DashboardResponse dashboard() { return dashboard.getDashboard(); }
    @GetMapping("/students")
    public List<StudentResponse> students() { return students.all(); }
    @GetMapping("/students/{studentId}")
    public StudentResponse student(@PathVariable @Positive Long studentId) { return students.get(studentId); }
    @GetMapping("/students/{studentId}/skills")
    public List<StudentSkillResponse> studentSkills(@PathVariable @Positive Long studentId) { return students.skills(studentId); }
}
