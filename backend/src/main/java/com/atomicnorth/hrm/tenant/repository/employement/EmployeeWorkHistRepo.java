package com.atomicnorth.hrm.tenant.repository.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeWorkHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeWorkHistRepo extends JpaRepository<EmployeeWorkHistory, Integer> {

    List<EmployeeWorkHistory> findByUserName(Integer userName);
}
