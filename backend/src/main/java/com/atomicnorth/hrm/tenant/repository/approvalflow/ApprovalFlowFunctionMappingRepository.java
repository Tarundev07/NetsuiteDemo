package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowFunctionMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalFlowFunctionMappingRepository extends JpaRepository<ApprovalFlowFunctionMapping, Integer> {

    List<ApprovalFlowFunctionMapping> findByApprovalFlowIdAndIsActive(Integer approvalId, String status);

    List<ApprovalFlowFunctionMapping> findByFunctionIdAndIsActive(Integer FunctionId, String Status);

    List<ApprovalFlowFunctionMapping> findByFunctionId(Integer functionId);
}
