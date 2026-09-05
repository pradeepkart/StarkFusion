package com.skillgap.analyzer.repository;

import com.skillgap.analyzer.entity.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserEmail(String email);
}

