package com.skillgap.analyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "applications", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "job_id"}), check = @CheckConstraint(constraint = "match_percent between 0 and 100"))
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "match_percent", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal matchPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    public Application() {}
    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public java.math.BigDecimal getMatchPercent() { return matchPercent; }
    public void setMatchPercent(java.math.BigDecimal matchPercent) { this.matchPercent = matchPercent; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
}
