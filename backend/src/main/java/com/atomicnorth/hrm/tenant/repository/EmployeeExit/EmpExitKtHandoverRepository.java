package com.atomicnorth.hrm.tenant.repository.EmployeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitKtHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpExitKtHandoverRepository extends JpaRepository<EmpExitKtHandover, Integer> {
    Optional<EmpExitKtHandover> findByExitRequestId(Integer exitRequestId);

    boolean existsByExitRequestId(Integer exitRequestId);





}
