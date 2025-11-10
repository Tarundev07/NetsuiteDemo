package com.atomicnorth.hrm.tenant.repository.reimbursement;

import com.atomicnorth.hrm.tenant.domain.reimbursement.ReimbursementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReimbursementRequestRepository extends JpaRepository<ReimbursementRequest, Integer>, JpaSpecificationExecutor<ReimbursementRequest> {

    @Query(value = "select Request_Number from ses_m00_rms_request where employee_id=:employeeId order by Sr_No desc limit 1", nativeQuery = true)
    String findReimbursementRequestNumber(@Param("employeeId") Integer employeeId);

    List<ReimbursementRequest> findByEmployeeId(Integer employeeId);

    Optional<ReimbursementRequest> findByRequestNumber(String requestNumber);
}