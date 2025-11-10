package com.atomicnorth.hrm.tenant.service.employeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitApproval;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class EmpExitApprovalService {

    @Autowired
    private EmpExitApprovalRepository approvalRepository;

    @Transactional
    public void createApproval(Integer exitRequestId, Integer approverId) {
        EmpExitApproval approval = new EmpExitApproval();
        approval.setExitRequestId(exitRequestId);
        approval.setApproverId(approverId);
        approval.setApprovalStatus("Pending");
        approvalRepository.save(approval);
    }

    public boolean existsByExitRequestId(Integer exitRequestId) {
        return approvalRepository.existsByExitRequestId(exitRequestId);
    }

}
