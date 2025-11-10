package com.atomicnorth.hrm.tenant.service.dto.employement.skillSetDropdown;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class EmployeesSkillSetDTO extends AbstractAuditingEntity<Long> implements Serializable {

    private Integer employeeSkillId;
    private Integer skillId;
    private String skillVersionId;
    private Integer username;
    private Date lastUsedDate;
    private Integer experienceInMonths;
    private String skillProficiencyCode;
    private String skillCategoryName;
    private String skillCategoryCode;
    private String isDeleted;
    private String isActive;

}
