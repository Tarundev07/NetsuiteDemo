package com.atomicnorth.hrm.tenant.domain.reimbursement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "ses_m00_rms_type")
public class ReimbursementType extends AbstractAuditingEntity<Long> {

    @Id
    @Column(name = "EXPENSE_CODE")
    private String expensecode;
    @Column(name = "NAME")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "ENABLE_FLAG")
    private String enableflag;
}
