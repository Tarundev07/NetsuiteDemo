package com.atomicnorth.hrm.tenant.domain.approvalflow;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity(name="workflow_master")
public class ApprovalFlow extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="APPROVAL_FLOW_ID")
    public Integer approvalFlowId;
    @Column(name="APPROVAL_FLOW_NAME")
    public String approvalFlowName;
    @Column(name="APPROVAL_FLOW_CODE")
    public String approvalFlowCode;
    @Column(name="START_DATE")
    public LocalDate startDate;
    @Column(name="END_DATE")
    public LocalDate endDate;


}
