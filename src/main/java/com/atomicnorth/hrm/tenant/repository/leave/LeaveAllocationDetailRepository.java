package com.atomicnorth.hrm.tenant.repository.leave;

import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocationDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveAllocationDetailRepository extends JpaRepository<LeaveAllocationDetails, Long> {
    List<LeaveAllocationDetails> findByLeaveCode(String leaveCode);

    List<LeaveAllocationDetails> findByLeaveAllocation(LeaveAllocation leaveAllocation);
}
