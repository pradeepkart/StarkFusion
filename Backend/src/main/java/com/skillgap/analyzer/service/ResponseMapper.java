package com.skillgap.analyzer.service;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.entity.*;

final class ResponseMapper {
    private ResponseMapper() {}
    static StudentResponse student(Student s) {
        return new StudentResponse(s.getStudentId(), s.getName(), s.getEmail(), s.getUser().getId());
    }
    static SkillResponse skill(Skill s) { return new SkillResponse(s.getSkillId(), s.getName(), s.getCategory()); }
    static JobResponse job(Job j) { return new JobResponse(j.getJobId(), j.getCompany(), j.getTitle(), j.getLocation()); }
    static StudentSkillResponse studentSkill(StudentSkill s) {
        return new StudentSkillResponse(s.getId(), s.getSkill().getSkillId(), s.getSkill().getName(),
                s.getSkill().getCategory(), s.getProficiency());
    }
    static JobSkillResponse jobSkill(JobSkill s) {
        return new JobSkillResponse(s.getId(), s.getSkill().getSkillId(), s.getSkill().getName(), s.getRequiredLevel(), s.isMandatory());
    }
    static ApplicationResponse application(Application a) {
        return new ApplicationResponse(a.getId(), a.getStudent().getStudentId(), a.getStudent().getName(),
                a.getJob().getJobId(), a.getJob().getTitle(), a.getJob().getCompany(), a.getMatchPercent(), a.getStatus());
    }
}
