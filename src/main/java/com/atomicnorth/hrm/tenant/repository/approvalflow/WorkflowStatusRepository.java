package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowStatusRepository extends JpaRepository<WorkflowStatus, Integer> {

    List<WorkflowStatus> findByAssignTo(Integer empId);

    List<WorkflowStatus> findByWorkflowRequestId(Integer RequestId);
}
