package com.atomicnorth.hrm.tenant.repository.employeeGrade;

import com.atomicnorth.hrm.tenant.domain.employeeGrade.EmployeeGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeGradeRepository extends JpaRepository<EmployeeGrade, Integer>, JpaSpecificationExecutor<EmployeeGrade> {

    boolean existsByGradeCodeAndIdNot(String gradeCode, Integer id);

    boolean existsByGradeName(String gradeName);

    boolean existsByGradeNameAndIdNot(String gradeName, Integer id);
}