package com.atomicnorth.hrm.tenant.repository.asset;

import com.atomicnorth.hrm.tenant.domain.asset.EmpAssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpAssetAssignmentRepo extends JpaRepository<EmpAssetAssignment, Integer> {

    List<EmpAssetAssignment> findByEmployeeId(Integer employeeId);
}
