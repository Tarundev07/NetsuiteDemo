package com.atomicnorth.hrm.tenant.repository.attendance;

import com.atomicnorth.hrm.tenant.domain.attendance.EmpRosterRemark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface EmpRosterRemarkRepository extends JpaRepository<EmpRosterRemark, Integer> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ses_m07_emp_roster_remark (USER_NAME, SHIFT_ID, DATE, ASSIGNEE_REMARK, TEMP_LEAVE_FLAG, LAST_UPDATED_BY, CREATED_BY, CREATION_DATE) " +
            "VALUES (:username, :shiftId, :date, :assigneeRemark, :tempLeaveFlag, :lastUpdatedBy, :createdBy, :creationDate)", nativeQuery = true)
    void insertEmpRosterRemark(@Param("username") String username,
                               @Param("shiftId") String shiftId,
                               @Param("date") String date,
                               @Param("assigneeRemark") String assigneeRemark,
                               @Param("tempLeaveFlag") String tempLeaveFlag,
                               @Param("lastUpdatedBy") String lastUpdatedBy,
                               @Param("createdBy") String createdBy,
                               @Param("creationDate") String creationDate);

}
