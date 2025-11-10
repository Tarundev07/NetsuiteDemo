package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.LeaveRequest;
import com.atomicnorth.hrm.tenant.service.dto.TrackLeaveDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    Page<LeaveRequest> findAll(Specification<LeaveRequest> spec, Pageable pageable);

    List<LeaveRequest> findByEmpIdAndStatusIn(Integer empId, List<String> statuses);

    boolean existsByRequestNumber(String requestNumber);

    List<LeaveRequest> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(LocalDate endDate, LocalDate startDate, String status);

    Optional<LeaveRequest> findByRequestNumber(String requestNumber);

    List<LeaveRequest> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);

    List<LeaveRequest> findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Integer empId, LocalDate endDate, LocalDate startDate);

    List<LeaveRequest> findByEmpIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(Integer empId, LocalDate endDate, LocalDate startDate, String status);
}

