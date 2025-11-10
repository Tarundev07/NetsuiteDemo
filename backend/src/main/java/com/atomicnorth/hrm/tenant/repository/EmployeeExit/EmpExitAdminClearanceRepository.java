package com.atomicnorth.hrm.tenant.repository.EmployeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitAdminClearance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpExitAdminClearanceRepository extends JpaRepository<EmpExitAdminClearance, Integer> {
    Optional<EmpExitAdminClearance> findByExitRequestId(Integer exitRequestId);

    boolean existsByExitRequestId(Integer exitRequestId);
}
