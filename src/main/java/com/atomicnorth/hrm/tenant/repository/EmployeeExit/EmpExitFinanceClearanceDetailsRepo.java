package com.atomicnorth.hrm.tenant.repository.EmployeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitFinanceClearanceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpExitFinanceClearanceDetailsRepo extends JpaRepository<EmpExitFinanceClearanceDetails, Integer> {
    List<EmpExitFinanceClearanceDetails> findByExitFinanceClearance_Id(Integer financeClearanceId);
}
