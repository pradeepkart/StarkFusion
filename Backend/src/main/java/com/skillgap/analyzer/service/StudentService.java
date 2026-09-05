package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.entity.*;
import com.skillgap.analyzer.exception.ResourceNotFoundException;
import com.skillgap.analyzer.repository.*;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository students;
    private final StudentSkillRepository studentSkills;
    private final SkillRepository skills;
    private final RecommendationService recommendations;
    public StudentService(StudentRepository students, StudentSkillRepository studentSkills,
                          SkillRepository skills, RecommendationService recommendations) {
        this.students = students; this.studentSkills = studentSkills;
        this.skills = skills; this.recommendations = recommendations;
    }

    public Student currentStudent(String email) {
        return students.findByUserEmail(email).orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }
    public StudentResponse profile(String email) { return ResponseMapper.student(currentStudent(email)); }
    public List<StudentResponse> all() {
        return students.findAll(Sort.by("studentId")).stream().map(ResponseMapper::student).toList();
    }
    public StudentResponse get(Long id) { return ResponseMapper.student(find(id)); }
    public List<StudentSkillResponse> skills(Long id) {
        find(id);
        return studentSkills.findByStudentStudentIdOrderBySkillSkillId(id).stream().map(ResponseMapper::studentSkill).toList();
    }

    @Transactional
    public StudentSkillResponse upsert(String email, StudentSkillRequest request) {
        Student student = currentStudent(email);
        Skill skill = skills.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id " + request.skillId()));
        StudentSkill assignment = studentSkills.findByStudentStudentIdAndSkillSkillId(student.getStudentId(), skill.getSkillId())
                .orElseGet(StudentSkill::new);
        assignment.setStudent(student);
        assignment.setSkill(skill);
        assignment.setProficiency(request.proficiency());
        studentSkills.saveAndFlush(assignment);
        recommendations.refreshStudent(student);
        return ResponseMapper.studentSkill(assignment);
    }

    @Transactional
    public void deleteSkill(String email, Long skillId) {
        Student student = currentStudent(email);
        StudentSkill assignment = studentSkills.findByStudentStudentIdAndSkillSkillId(student.getStudentId(), skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Student skill not found with skill id " + skillId));
        studentSkills.delete(assignment);
        studentSkills.flush();
        recommendations.refreshStudent(student);
    }

    private Student find(Long id) {
        return students.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }
}
