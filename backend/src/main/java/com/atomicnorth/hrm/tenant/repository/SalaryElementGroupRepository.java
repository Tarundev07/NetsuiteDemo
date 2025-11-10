package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.SalaryElementGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryElementGroupRepository extends JpaRepository<SalaryElementGroup, Long>, JpaSpecificationExecutor<SalaryElementGroup> {

    Optional<SalaryElementGroup> findByGroupNameIgnoreCaseAndCompany(String groupName, Long company);

    List<SalaryElementGroup> findByIsActiveTrueOrderByGroupNameAsc();
}
