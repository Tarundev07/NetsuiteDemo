package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectAllocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectAllocationHistoryRepo extends JpaRepository<ProjectAllocationHistory, Integer> {
    List<ProjectAllocationHistory> findByProjectAllocationId(Integer projectAllocationId);

    List<ProjectAllocationHistory> findByEmployeeId(Integer employeeId);
}
