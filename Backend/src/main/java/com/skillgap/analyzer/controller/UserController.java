package com.skillgap.analyzer.controller;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.service.StudentService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {
    private final StudentService students;
    public UserController(StudentService students) { this.students = students; }
    @GetMapping("/profile")
    public StudentResponse profile(Principal principal) { return students.profile(principal.getName()); }
    @GetMapping("/skills")
    public List<StudentSkillResponse> skills(Principal principal) {
        return students.skills(students.currentStudent(principal.getName()).getStudentId());
    }
    @PostMapping("/skills")
    public StudentSkillResponse upsert(Principal principal, @Valid @RequestBody StudentSkillRequest request) {
        return students.upsert(principal.getName(), request);
    }
    @DeleteMapping("/skills/{skillId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable @Positive Long skillId) { students.deleteSkill(principal.getName(), skillId); }
}
