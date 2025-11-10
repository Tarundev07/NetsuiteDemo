package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowLevelMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ApprovalFlowLevelMappingRepository extends JpaRepository<ApprovalFlowLevelMapping, Integer> {

    List<ApprovalFlowLevelMapping> findByApprovalFlowLevelIdOrderByDisplayOrder(Integer approvalFlowLevelId);

    List<ApprovalFlowLevelMapping> findByApprovalFlowLevelIdIn(Set<Integer> levelIds);

    void deleteByApprovalFlowLevelIdAndDesignationId(Integer approvalFlowLevelId, Integer id);
}
