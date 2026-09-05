package com.skillgap.analyzer.repository;

import com.skillgap.analyzer.entity.StudentSkill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {
    @EntityGraph(attributePaths = "skill")
    List<StudentSkill> findByStudentStudentIdOrderBySkillSkillId(Long studentId);
    Optional<StudentSkill> findByStudentStudentIdAndSkillSkillId(Long studentId, Long skillId);
    boolean existsBySkillSkillId(Long skillId);
}

