package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.SalaryElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryElementRepository extends JpaRepository<SalaryElement, Long> {

}
