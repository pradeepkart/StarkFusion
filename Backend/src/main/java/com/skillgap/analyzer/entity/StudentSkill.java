package com.skillgap.analyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_skills", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "skill_id"}), check = @CheckConstraint(constraint = "proficiency between 1 and 5"))
public class StudentSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private int proficiency;

    public StudentSkill() {}
    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
    public int getProficiency() { return proficiency; }
    public void setProficiency(int proficiency) { this.proficiency = proficiency; }
}
