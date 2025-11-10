package com.atomicnorth.hrm.tenant.service.dto.employement.viewDTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeSkillViewDTO implements Serializable {

    private Integer employeeSkillId;
    private Integer username;
    private String lastUsedDate;
    private Integer experienceInMonth;
    private String proficiencyMeaning;
    private String skillProficiencyCode;
    private String categoryMeaning;
    private String skillCategoryCode;
    private String isDeleted;
    private String isActive;
    private String lastUpdatedBy;
    private String createdBy;
    private String lastUpdateDate;
    private String creationDate;
    private String skillName;
    private String description;
    private Integer skillVersionId;
    private String version;
    private Integer skillId;

}
