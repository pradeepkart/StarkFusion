package com.skillgap.analyzer.controller;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.service.SkillService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class SkillController {
    private final SkillService skills;
    public SkillController(SkillService skills) { this.skills = skills; }
    @GetMapping({"/api/skills", "/api/admin/skills"})
    public List<SkillResponse> all() { return skills.all(); }
    @GetMapping("/api/skills/{skillId}")
    public SkillResponse get(@PathVariable @Positive Long skillId) { return skills.get(skillId); }
    @PostMapping("/api/admin/skills") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
    public SkillResponse create(@Valid @RequestBody SkillRequest request) { return skills.create(request); }
    @PutMapping("/api/admin/skills/{skillId}") @PreAuthorize("hasRole('ADMIN')")
    public SkillResponse update(@PathVariable @Positive Long skillId, @Valid @RequestBody SkillRequest request) {
        return skills.update(skillId, request);
    }
    @DeleteMapping("/api/admin/skills/{skillId}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable @Positive Long skillId) { skills.delete(skillId); }
}
