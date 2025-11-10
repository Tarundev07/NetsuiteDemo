package com.atomicnorth.hrm.tenant.repository.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeAddressRepo extends JpaRepository<EmployeeAddress, Integer> {
    List<EmployeeAddress> findByUsername(Integer username);

}