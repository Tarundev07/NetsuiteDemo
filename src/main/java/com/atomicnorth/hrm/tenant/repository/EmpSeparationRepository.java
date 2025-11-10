package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.EmpSeparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpSeparationRepository extends JpaRepository<EmpSeparation, Long> {

}
