package com.atomicnorth.hrm.tenant.domain.approvalflow;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Data
@Entity(name="workflow_delegation")
public class ApprovalFlowDelegation extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Integer id;
    @Column(name="APPROVAL_FLOW_ID")
    private Integer approvalFlowId;
    @Column(name="EMPLOYEE_ID")
    private Integer employeeId;
    @Column(name="DELEGATION_ID")
    private Integer delegationId;
    @Column(name="IS_ACTIVE")
    private String isActive;
    @Column(name="START_DATE")
    public LocalDate startDate;
    @Column(name="END_DATE")
    public LocalDate  endDate;



}
