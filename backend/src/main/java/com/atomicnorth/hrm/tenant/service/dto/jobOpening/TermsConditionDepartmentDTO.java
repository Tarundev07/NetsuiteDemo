package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

@Data
public class TermsConditionDepartmentDTO extends AbstractAuditingEntity<Integer> {

    private Integer id;

    private Integer termConditionId;

    private Integer departmentId;

    private String departmentName;

    private String isActive;

}
