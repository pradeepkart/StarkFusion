package com.skillgap.analyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skills")
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Long skillId;

    @Column(nullable = false, length = 120, unique = true)
    private String name;

    @Column(nullable = false, length = 120)
    private String category;

    public Skill() {}
    public Long getSkillId() { return skillId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
