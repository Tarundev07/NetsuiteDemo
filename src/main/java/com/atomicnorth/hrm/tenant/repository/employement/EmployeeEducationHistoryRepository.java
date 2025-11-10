package com.atomicnorth.hrm.tenant.repository.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeEducationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeEducationHistoryRepository extends JpaRepository<EmployeeEducationHistory, Integer> {

    List<EmployeeEducationHistory> findByUserName(Integer userName);

}
