package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.EmployeeHierarchyView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeHierarchyViewRepository extends JpaRepository<EmployeeHierarchyView, Long> {
    Optional<EmployeeHierarchyView> findByEmployeeId(Integer employeeId);
}
