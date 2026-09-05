package com.skillgap.analyzer.repository;

import com.skillgap.analyzer.entity.Skill;
import org.springframework.data.jpa.repository.*;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndSkillIdNot(String name, Long skillId);
}

