package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface ApprovalFlowRepository extends JpaRepository<ApprovalFlow, Integer>, JpaSpecificationExecutor<ApprovalFlow> {
    Page<ApprovalFlow> findByApprovalFlowNameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<ApprovalFlow> findByApprovalFlowCodeContainingIgnoreCase(String searchValue, Pageable pageable);

    boolean existsByApprovalFlowCodeIgnoreCase(String approvalFlowCode);

    boolean existsByApprovalFlowCodeIgnoreCaseAndApprovalFlowIdNot(String approvalFlowCode, Integer approvalFlowId);

    ApprovalFlow findByApprovalFlowId(Integer approvalFlowId);
}
