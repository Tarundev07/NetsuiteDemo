package com.atomicnorth.hrm.tenant.repository.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EmployeeGroupRepo extends JpaRepository<EmployeeGroup, Long>, JpaSpecificationExecutor<EmployeeGroup> {

    List<EmployeeGroup> findByGroupId(Long groupId);

    Optional<EmployeeGroup> findByGroupIdAndEmpId(Long groupId, String empId);
}
