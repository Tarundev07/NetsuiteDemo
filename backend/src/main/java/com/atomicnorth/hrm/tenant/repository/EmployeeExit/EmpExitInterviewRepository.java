package com.atomicnorth.hrm.tenant.repository.EmployeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpExitInterviewRepository extends JpaRepository<EmpExitInterview, Integer> {
    Optional<EmpExitInterview> findByExitRequestId(Integer exitRequestId);

    boolean existsByExitRequestId(Integer exitRequestId);
}
