package com.atomicnorth.hrm.tenant.repository.attendance;

import com.atomicnorth.hrm.tenant.domain.attendance.AttendanceMoaf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository

public interface AttendanceMoafRepo extends JpaRepository<AttendanceMoaf, Integer>, JpaSpecificationExecutor<AttendanceMoaf> {

    @Query(value = "SELECT TRIGGER_FLAG FROM ses_m00_tms_email_event_trigger_v WHERE EVENT_ID = :eventid", nativeQuery = true)
    String findTriggerFlagByEventId(@Param("eventid") String eventId);

    Optional<AttendanceMoaf> findByEmployeeIdAndMoafDateAndStatus(Integer employeeId, LocalDate moafDate, String status);

    @EntityGraph(attributePaths = "employee")
    Page<AttendanceMoaf> findByEmployeeId(Integer id, Pageable pageable);

    Page<AttendanceMoaf> findByEmployee_EmployeeNumberContainingIgnoreCaseAndEmployeeId(String searchKeyword, Integer employeeId, Pageable pageable);

    Page<AttendanceMoaf> findByEmployee_FirstNameContainingIgnoreCaseAndEmployeeId(String searchKeyword, Integer employeeId, Pageable pageable);

    Page<AttendanceMoaf> findByEmployeeIdAndMoafDateBetween(Integer employeeId, LocalDate firstDate, LocalDate LastDate, Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    List<AttendanceMoaf> findByEmployee_EmployeeIdInAndMoafDateBetweenAndStatus(Set<Integer> employeeIds, LocalDate firstDate, LocalDate lastDate, String status);

    @EntityGraph(attributePaths = "employee")
    List<AttendanceMoaf> findByMoafDateBetweenAndStatus(LocalDate firstDate, LocalDate lastDate, String status);

    Optional<AttendanceMoaf> findByFormRfNum(Integer formRfNum);

    Optional<AttendanceMoaf> findByRequestNumber(String requestNumber);
}
