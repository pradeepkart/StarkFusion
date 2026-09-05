package com.skillgap.analyzer.repository;

import com.skillgap.analyzer.entity.Application;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByStudentStudentIdAndJobJobId(Long studentId, Long jobId);
    boolean existsByJobJobId(Long jobId);
    @EntityGraph(attributePaths = {"student", "job"})
    List<Application> findByStudentStudentIdOrderByIdDesc(Long studentId);
    @EntityGraph(attributePaths = {"student", "job"})
    List<Application> findAllByOrderByIdDesc();
    @Query("select avg(a.matchPercent) from Application a")
    Double averageMatchPercent();
}

