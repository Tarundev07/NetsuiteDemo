package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.LeaveAllocationDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveAllocationRepository extends JpaRepository<LeaveAllocation, Long> {

    Optional<LeaveAllocation> findByEmpIdAndIsActiveAndLeaveAllocationDetails_LeaveCode(Integer empId, String isActive, String leaveType);
    
    boolean existsByEmpId(Integer empId);

    Page<LeaveAllocation> findAll(Specification<LeaveAllocation> spec, Pageable pageable);
    Optional<LeaveAllocation> findByEmpId(Integer empId);
    List<LeaveAllocation> findAllByEmpIdIn(List<Integer> employeeIds);
}
