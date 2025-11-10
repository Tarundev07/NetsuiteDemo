package com.atomicnorth.hrm.tenant.repository.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EmployeeAccountRepo extends JpaRepository<EmployeeAccountEntity, Integer>, JpaSpecificationExecutor<EmployeeAccountEntity> {

    Optional<EmployeeAccountEntity> findByAccountNumber(String accountNumber);

    List<EmployeeAccountEntity> findByEmployeeId(Integer employeeId);
}