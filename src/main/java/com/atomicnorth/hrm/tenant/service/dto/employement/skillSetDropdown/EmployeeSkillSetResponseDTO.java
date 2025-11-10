package com.atomicnorth.hrm.tenant.service.dto.employement.skillSetDropdown;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class EmployeeSkillSetResponseDTO {

    @JsonProperty("EMPLOYEE_SKILL_ID")
    private Integer employeeSkillId;

    @JsonProperty("USER_NAME")
    private Integer username;

    @JsonProperty("LAST_USED_DATE")
    private Date lastUsedDate;

    @JsonProperty("EXPERIENCE_IN_MONTH")
    private Integer experienceInMonths;

    @JsonProperty("PROFICIENCY_MEANING")
    private String proficiencyMeaning;

    @JsonProperty("SKILL_PROFICIENCY_CODE")
    private String skillProficiencyCode;

    @JsonProperty("CATEGORY_MEANING")
    private String categoryMeaning;

    @JsonProperty("SKILL_CATEGORY_CODE")
    private String skillCategoryCode;

    @JsonProperty("IS_DELETED")
    private String isDeleted;

    @JsonProperty("IS_ACTIVE")
    private String isActive;

    @JsonProperty("LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    @JsonProperty("CREATION_DATE")
    private Date creationDate;

    @JsonProperty("SKILL_NAME")
    private String skillName;

    @JsonProperty("DESCRIPTION")
    private String description;

    @JsonProperty("SKILL_ID")
    private Integer skillId;

    @JsonProperty("SKILL_VERSION_ID")
    private Integer skillVersionId;

    @JsonProperty("VERSION")
    private String version;
}
