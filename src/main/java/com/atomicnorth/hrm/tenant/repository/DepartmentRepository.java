package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.Department;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByDnameIgnoreCaseAndCompany(String dname, Long company);

    Optional<Department> findById(Long id);

    @Query(value = "SELECT D_NAME FROM ses_m04_department WHERE ID = :departmentId",
            nativeQuery = true)
    Optional<String> findDepartmentNameById(@Param("departmentId") Long departmentId);

    @Query(value = "SELECT ID, D_NAME FROM ses_m04_department d ORDER BY d.D_NAME ASC",
            nativeQuery = true)
    List<Object[]> findAllDepartmentIdAndName();

}