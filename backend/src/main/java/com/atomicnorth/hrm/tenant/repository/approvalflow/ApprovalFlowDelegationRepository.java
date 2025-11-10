package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowDelegation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalFlowDelegationRepository extends JpaRepository<ApprovalFlowDelegation,Integer> {

    List<ApprovalFlowDelegation> findByApprovalFlowIdAndIsActive(Integer approvalId, String status);

    List<ApprovalFlowDelegation> findByApprovalFlowId(Integer approvalId);
}
