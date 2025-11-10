package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.EmployeeHierarchyView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeHierarchyViewRepo extends JpaRepository<EmployeeHierarchyView, Integer> {

    List<EmployeeHierarchyView> findByDivisionIdIn(List<Integer> divisionId);

    List<EmployeeHierarchyView> findByDivisionIdInAndDepartmentIdIn(List<Integer> divisionId, List<Integer> departmentId);

    List<EmployeeHierarchyView> findByDivisionIdInAndDepartmentIdInAndReportingManagerIdIn(List<Integer> divisionId, List<Integer> departmentId, List<Integer> reportingManagerId);

    List<EmployeeHierarchyView> findByDepartmentIdIn(List<Integer> departmentId);

    List<EmployeeHierarchyView> findByDepartmentIdInAndReportingManagerIdIn(List<Integer> departmentId, List<Integer> reportingManagerId);

    List<EmployeeHierarchyView> findByReportingManagerIdIn(List<Integer> reportingManagerId);

    List<EmployeeHierarchyView> findByDivisionIdInAndReportingManagerIdIn(List<Integer> divisionId, List<Integer> reportingManagerId);
}
