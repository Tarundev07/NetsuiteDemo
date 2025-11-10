package com.atomicnorth.hrm.tenant.repository.employement;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeFamilyRepo extends JpaRepository<EmployeeFamily, Integer> {

    List<EmployeeFamily> findByUserName(Integer userName);
}
