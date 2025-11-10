package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowRequestMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalFlowRequestMappingRepository extends JpaRepository<ApprovalFlowRequestMapping,Integer> {

    List<ApprovalFlowRequestMapping> findByApprovalFlowIdAndIsActive(Integer approvalId, String status);

    List<ApprovalFlowRequestMapping> findByApprovalFlowIdInAndIsActive(List<Integer> approvalId, String status);
    List<ApprovalFlowRequestMapping> findByApprovalFlowIdAndRequestedByAndRequestIdAndIsActive(Integer approvalFlowId, String requestedBy, String requestId, String y);
}
