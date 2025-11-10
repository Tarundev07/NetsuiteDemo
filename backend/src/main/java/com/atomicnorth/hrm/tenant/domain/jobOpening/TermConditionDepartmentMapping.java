package com.atomicnorth.hrm.tenant.domain.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ses_m00_terms_condition_department_mapping")
public class TermConditionDepartmentMapping extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "TERM_CONDITION_ID", nullable = false)
    private Integer termsConditionId;

    @Column(name = "DEPARTMENT_ID", nullable = false)
    private Integer departmentId;

    @Column(name = "IS_ACTIVE", nullable = false)
    private String isActive;
}