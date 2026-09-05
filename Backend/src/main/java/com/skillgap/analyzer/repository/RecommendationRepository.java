package com.skillgap.analyzer.repository;

import com.skillgap.analyzer.entity.Recommendation;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    @EntityGraph(attributePaths = "skill")
    List<Recommendation> findByStudentStudentIdAndJobJobId(Long studentId, Long jobId);
    void deleteByJobJobId(Long jobId);
}

