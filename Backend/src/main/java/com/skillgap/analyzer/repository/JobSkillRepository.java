package com.skillgap.analyzer.repository;

import com.skillgap.analyzer.entity.JobSkill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    @EntityGraph(attributePaths = "skill")
    List<JobSkill> findByJobJobIdOrderBySkillSkillId(Long jobId);
    Optional<JobSkill> findByJobJobIdAndSkillSkillId(Long jobId, Long skillId);
    boolean existsBySkillSkillId(Long skillId);
    void deleteByJobJobId(Long jobId);
}

