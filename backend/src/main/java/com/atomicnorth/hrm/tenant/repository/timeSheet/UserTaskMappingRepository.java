package com.atomicnorth.hrm.tenant.repository.timeSheet;

import com.atomicnorth.hrm.tenant.domain.timeSheet.UserTaskMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserTaskMappingRepository extends JpaRepository<UserTaskMapping, Long> {

    List<UserTaskMapping> findDistinctTaskIdByUsernameAndTimesheetDateBetween(String username, Date startDate, Date endDate);

    List<UserTaskMapping> findByUsernameAndTaskIdAndTimesheetDateBetweenAndTimesheetStatusNotOrderByTimesheetDateAsc(String username, String taskId, Date startDate, Date endDate, String status);

    @Query(value = "SELECT count(1) FROM ses_m25_emp_current_work_info_v WHERE USER_NAME = :username AND (REPORTING_PERSON = :reportingPerson OR HR_MANAGER = :hrManager)", nativeQuery = true)
    boolean countByUsernameAndReportingPersonOrHrManager(@Param("username") String username, @Param("reportingPerson") String reportingPerson, @Param("hrManager") String hrManager);

    @Query(value = "select t.TIMESHEET_ID ,t.PROJECT_ID,t.TASK_ID,t.TIMESHEET_DATE,t.EFFORT_IN_HOURS,t.TIMESHEET_STATUS," +
            "t.REMARK,t.USER_NAME,t.APPROVER_REMARK,t.ACCOUNT_MANAGER_REMARK,t.BILLABLE_EFFORT_IN_HOURS,t.BIILABLE_FLAG,(select PROJECT_NAME from ses_m02_project where" +
            " PROJECT_RF_NUM=t.PROJECT_ID) as PROJECT_NAME,(select TASK_NAME from ses_m02_task_story where " +
            "TASK_RF_NUM=t.TASK_ID) as Taskname,((SELECT CONCAT_WS(' ', FIRST_NAME, LAST_NAME) FROM emp_employee_master WHERE EMPLOYEE_ID = (select user_Type_id from ses_m04_user_association where USER_ID = t.USER_NAME))) as userfName from ehrms_tms_timesheet t where " +
            "t.USER_NAME=:username and TIMESHEET_DATE" +
            " between :startDate and :endDate   AND t.timesheet_status != 'deleted' order by TIMESHEET_DATE asc", nativeQuery = true)
    List<Object[]> getUserTaskMappings(
            @Param("username") String username,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE ehrms_tms_timesheet " +
            "SET TIMESHEET_STATUS = :status, SUBMIT_STATUS = 'true', " +
            "LAST_UPDATED_DATE = :modifiedDate, LAST_UPDATED_BY = :modifiedBy " +
            "WHERE USER_NAME = :username AND TIMESHEET_DATE BETWEEN :startDate AND :endDate")
    void updateUserTaskMappingStatus(@Param("status") String status,
                                     @Param("modifiedDate") String modifiedDate,
                                     @Param("modifiedBy") String modifiedBy,
                                     @Param("username") String username,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE ehrms_tms_timesheet " +
            "SET TIMESHEET_STATUS = :status, SUBMIT_STATUS = 'false', " +
            "LAST_UPDATED_DATE = :modifiedDate, LAST_UPDATED_BY = :modifiedBy " +
            "WHERE USER_NAME = :username AND TIMESHEET_DATE BETWEEN :startDate AND :endDate")
    void updateUserTaskMappingStatusWithFalse(@Param("status") String status,
                                              @Param("modifiedDate") String modifiedDate,
                                              @Param("modifiedBy") String modifiedBy,
                                              @Param("username") String username,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);


    Optional<UserTaskMapping> findByTimesheetId(Integer timesheetId);

    List<UserTaskMapping> findByEmployeeIdInAndTimesheetDateBetweenAndTimesheetStatusNot(List<Integer> employeeId, Date startDate, Date endDate, String status);

    List<UserTaskMapping> findByTimesheetDateBetween(Date startDate, Date endDate);

    List<UserTaskMapping> findByUsername(String username);

    List<UserTaskMapping> findByEmployeeIdAndTimesheetDateBetweenAndTimesheetStatus(Integer employeeId, Date startDate, Date endDate, String status);
}