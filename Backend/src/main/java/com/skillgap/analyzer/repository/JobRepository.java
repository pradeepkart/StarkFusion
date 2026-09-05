package com.skillgap.analyzer.repository;

import com.skillgap.analyzer.entity.Job;
import org.springframework.data.jpa.repository.*;

public interface JobRepository extends JpaRepository<Job, Long> {
    
}

