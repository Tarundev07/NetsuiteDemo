package com.atomicnorth.hrm.tenant.domain.approvalflow;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity(name = "workflow_level_emp")
public class ApprovalFlowLevelMapping extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WORK_FLOW_LEVEL_MAPPING_ID")
    private Integer approvalFlowLevelMappingId;
    @Column(name = "WORK_FLOW_LEVEL_ID")
    private Integer approvalFlowLevelId;
    @Column(name = "DESIGNATION_ID")
    private Integer designationId;
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;
    @Column(name = "ORDER_BY")
    private Integer displayOrder;
    @Column(name = "MAIL_ACTIVE")
    private String mailActive;
    @Column(name = "SMS_ACTIVE")
    private String smsActive;
    @Column(name = "IS_ACTIVE")
    private String isActive;
}
