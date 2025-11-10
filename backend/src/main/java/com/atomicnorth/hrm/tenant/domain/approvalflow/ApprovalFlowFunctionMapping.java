package com.atomicnorth.hrm.tenant.domain.approvalflow;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name="workflow_function_mapping")
public class ApprovalFlowFunctionMapping extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="WORK_FLOW_FUNCTION_MAPPING_ID")
    private Integer workFlowFunctionMappingId;

    @Column(name="APPROVAL_FLOW_ID")
    private Integer approvalFlowId;
    @Column(name="FUNCTION_ID")
    private Integer functionId;
    @Column(name="BY_PASS_FLAG")
    private String byPassFlag;
    @Column(name="IS_ACTIVE")
    private String isActive;
}
