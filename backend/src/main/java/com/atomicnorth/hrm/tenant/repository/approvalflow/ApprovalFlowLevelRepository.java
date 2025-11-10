package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalFlowLevelRepository extends JpaRepository<ApprovalFlowLevel,Integer> {
    List<ApprovalFlowLevel> findByApprovalFlowIdAndIsActive(Integer approvalId, String status);

    List<ApprovalFlowLevel> findByApprovalFlowIdAndIsActiveOrderByLevelMasterId(Integer approvalId, String status);
}