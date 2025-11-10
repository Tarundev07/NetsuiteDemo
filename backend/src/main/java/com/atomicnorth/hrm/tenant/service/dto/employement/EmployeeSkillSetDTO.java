package com.atomicnorth.hrm.tenant.service.dto.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillSetDTO extends AbstractAuditingEntity<Long> implements Serializable {

    private Integer employeeSkillId;
    private Integer skillId;
    private String skillVersionId;
    private Integer username;
    private Date lastUsedDate;
    private Integer experienceInMonths;
    private String skillProficiencyCode;
    private String skillCategoryName;
    private String skillCategoryCode;
    private String isActive;
}
