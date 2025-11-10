package com.atomicnorth.hrm.tenant.repository.EmployeeExit;

import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpExitApprovalRepository extends JpaRepository<EmpExitApproval, Integer> {

    Page<EmpExitApproval> findAll(Specification<EmpExitApproval> spec, Pageable pageable);

    Page<EmpExitApproval> findByApproverId(Integer id, Pageable pageable);


    boolean existsByExitRequestId(Integer exitRequestId);

    Optional<EmpExitApproval> findByExitRequestId(Integer id);

}
