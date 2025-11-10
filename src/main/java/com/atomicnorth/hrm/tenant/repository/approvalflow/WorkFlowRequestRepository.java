package com.atomicnorth.hrm.tenant.repository.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkFlowRequestRepository extends JpaRepository<WorkflowRequest, Integer> {

    Optional<WorkflowRequest> findByRequestNumber(String requestNumber);

}
