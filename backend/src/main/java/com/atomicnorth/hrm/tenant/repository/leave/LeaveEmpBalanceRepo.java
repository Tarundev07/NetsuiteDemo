package com.atomicnorth.hrm.tenant.repository.leave;

import com.atomicnorth.hrm.tenant.domain.leave.LeaveEmpBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveEmpBalanceRepo extends JpaRepository<LeaveEmpBalance, Integer> {

    Optional<LeaveEmpBalance> findByEmployeeIdAndLeaveCode(Integer employeeId, Integer leaveCode);
}
