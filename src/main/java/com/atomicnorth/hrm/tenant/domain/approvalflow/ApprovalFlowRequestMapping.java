package com.atomicnorth.hrm.tenant.domain.approvalflow;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name="workflow_request_mapping")
public class ApprovalFlowRequestMapping extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="WORK_FLOW_REQUEST_MAPPING_ID")
    private Integer workFlowRequestMappingId;
    @Column(name="APPROVAL_FLOW_ID")
    private Integer approvalFlowId;
    @Column(name="REQUESTED_BY")
    private String requestedBy;
    @Column(name="REQUEST_ID")
    private String requestId;
    @Column(name="BY_PASS_FLAG")
    private String byPassFlag;
    @Column(name="IS_ACTIVE")
    private String isActive;

}
