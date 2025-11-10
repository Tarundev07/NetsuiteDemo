package com.atomicnorth.hrm.tenant.repository.EmployeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpExitRequestRepository extends JpaRepository<EmpExitRequest, Integer> {

    List<EmpExitRequest> findByEmployeeId(Integer employeeId);

    Optional<EmpExitRequest> findFirstByEmployeeId(Integer employeeId);

    Optional<EmpExitRequest> findTop1ByEmployeeIdOrderByIdDesc(Integer employeeId);

    List<EmpExitRequest> findAll();
  
}

