package com.atomicnorth.hrm.tenant.repository.EmployeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFullFinalSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpExitFullFinalSettlementRepository extends JpaRepository<EmpExitFullFinalSettlement, Integer> {
    Optional<EmpExitFullFinalSettlement> findByExitRequestId(Integer exitRequestId);

    boolean existsByExitRequestId(Integer exitRequestId);
}
