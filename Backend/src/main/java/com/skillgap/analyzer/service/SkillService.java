package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.entity.Skill;
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
public class SkillService {
    private final SkillRepository skills;
    private final StudentSkillRepository studentSkills;
    private final JobSkillRepository jobSkills;
    private final RecommendationService recommendations;
    public SkillService(SkillRepository skills, StudentSkillRepository studentSkills,
                        JobSkillRepository jobSkills, RecommendationService recommendations) {
        this.skills = skills; this.studentSkills = studentSkills; this.jobSkills = jobSkills; this.recommendations = recommendations;
    }
    public List<SkillResponse> all() { return skills.findAll(Sort.by("skillId")).stream().map(ResponseMapper::skill).toList(); }
    public SkillResponse get(Long id) { return ResponseMapper.skill(find(id)); }

    @Transactional
    public SkillResponse create(SkillRequest request) {
        if (skills.existsByNameIgnoreCase(request.name().strip())) throw duplicate();
        Skill skill = new Skill();
        skill.setName(request.name().strip());
        skill.setCategory(request.category().strip());
        return ResponseMapper.skill(skills.saveAndFlush(skill));
    }
    @Transactional
    public SkillResponse update(Long id, SkillRequest request) {
        Skill skill = find(id);
        if (skills.existsByNameIgnoreCaseAndSkillIdNot(request.name().strip(), id)) throw duplicate();
        skill.setName(request.name().strip());
        skill.setCategory(request.category().strip());
        skills.flush();
        recommendations.refreshAll();
        return ResponseMapper.skill(skill);
    }
    @Transactional
    public void delete(Long id) {
        Skill skill = find(id);
        if (studentSkills.existsBySkillSkillId(id) || jobSkills.existsBySkillSkillId(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Skill is in use; remove student and job assignments first");
        skills.delete(skill);
        skills.flush();
    }
    private Skill find(Long id) {
        return skills.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill not found with id " + id));
    }
    private ResponseStatusException duplicate() { return new ResponseStatusException(HttpStatus.CONFLICT, "Skill name already exists"); }
}
