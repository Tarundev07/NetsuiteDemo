package com.atomicnorth.hrm.tenant.domain.approvalflow;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity(name = "workflow_hierarchy")
public class ApprovalFlowLevel extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WORK_FLOW_LEVEL_ID")
    private Integer approvalFlowLevelId;
    @Column(name = "APPROVAL_FLOW_ID")
    private Integer approvalFlowId;
    @Column(name = "LEVEL_MASTER_ID")
    private Integer levelMasterId;
    @Column(name = "TURNAROUND_TIME")
    private Integer turnAroundTime;
    @Column(name = "BY_PASS_FLAG")
    private String byPassFlag;
    @Column(name = "IS_ACTIVE")
    private String isActive;
}