package com.atomicnorth.hrm.tenant.repository.EmployeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFinanceClearance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpExitFinanceClearanceRepository extends JpaRepository<EmpExitFinanceClearance, Integer> {
    Optional<EmpExitFinanceClearance> findByExitRequestId(Integer exitRequestId);

    boolean existsByExitRequestId(Integer exitRequestId);
}
