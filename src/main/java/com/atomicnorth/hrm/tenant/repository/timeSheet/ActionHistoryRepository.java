package com.atomicnorth.hrm.tenant.repository.timeSheet;

import com.atomicnorth.hrm.tenant.domain.timeSheet.ActionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ActionHistoryRepository extends JpaRepository<ActionHistory, Integer> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ses_m00_tms_action_history (ACTION_TYPE, USER_NAME, LAST_UPDATED_BY, REMARK, ADDED_ON, END_DATE, START_DATE) " +
            "VALUES (:actionType, :username, :lastModifiedBy, :remark, :addedOn, :endDate, :startDate)", nativeQuery = true)
    Integer insertActionHistory(@Param("actionType") String actionType,
                                @Param("username") String username,
                                @Param("lastModifiedBy") String lastModifiedBy,
                                @Param("remark") String remark,
                                @Param("addedOn") String addedOn,
                                @Param("endDate") String endDate,
                                @Param("startDate") String startDate);

    @Query(value = "DELETE FROM ses_m25_emp_attendance_logs"
            + "WHERE EMPID = :empId "
            + "AND deviceid = :deviceId "
            + "AND LOGTIME BETWEEN :startTime AND :endTime", nativeQuery = true)
    void deleteByEmpIdAndDeviceIdAndLogTimeRange(@Param("empId") String empId,
                                                 @Param("deviceId") String deviceId,
                                                 @Param("startTime") String startTime,
                                                 @Param("endTime") String endTime);

    @Query(value = "DELETE FROM ses_m25_emp_attendance_logs_archive " +
            "WHERE EMP_ID = :empId AND DEVICE_ID = :deviceId AND LOG_TIME BETWEEN :startDate AND :endDate", nativeQuery = true)
    void deleteLogsByEmpIdAndDeviceIdAndLogTimeRange(@Param("empId") String empId,
                                                     @Param("deviceId") String deviceId,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);

    @Query(
            value = "SELECT " +
                    " m.user_name AS user_name," +
                    "  SUM(m.effort_in_hours) AS week_effort," +
                    "  CONCAT_WS(' ', MIN(e.FIRST_NAME), MIN(e.LAST_NAME)) AS fname," +
                    "  MIN(e.EMPLOYEE_ID) AS employee_id," +
                    "  MIN(e.REPORTING_MANAGER_ID) AS reporting_manager_id," +
                    "  CONCAT_WS(' ', MIN(rm_master.FIRST_NAME), MIN(rm_master.LAST_NAME)) AS reporting_manager_name," +
                    "  MIN(m.approver_remark) AS approver_remark," +
                    "    MIN(m.TIMESHEET_STATUS) AS status, "+
                    "  m.week_start," +
                    "  CONCAT(DATE_FORMAT(m.week_start, '%Y-%m-%d'), ' - ', DATE_FORMAT(MIN(m.week_end), '%Y-%m-%d')) AS week_range ," +
                    "  MIN(m.TIMESHEET_ID) AS id " +
                    "FROM (" +
                    "  SELECT " +
                    "    t.TIMESHEET_ID," +
                    "    t.USER_NAME," +
                    "    t.EFFORT_IN_HOURS," +
                    "    t.TIMESHEET_DATE," +
                    "    t.TIMESHEET_STATUS," +
                    "    t.APPROVER_REMARK," +
                    "    CASE " +
                    "      WHEN DAYNAME(t.TIMESHEET_DATE) = 'Sunday' THEN t.TIMESHEET_DATE" +
                    "      ELSE SUBDATE(t.TIMESHEET_DATE, INTERVAL (WEEKDAY(t.TIMESHEET_DATE) + 1) DAY)" +
                    "    END AS week_start," +
                    "    CASE " +
                    "      WHEN DAYNAME(t.TIMESHEET_DATE) = 'Sunday' THEN DATE_ADD(t.TIMESHEET_DATE, INTERVAL 6 DAY)" +
                    "      ELSE DATE_ADD(SUBDATE(t.TIMESHEET_DATE, INTERVAL (WEEKDAY(t.TIMESHEET_DATE) + 1) DAY), INTERVAL 6 DAY)" +
                    "    END AS week_end" +
                    "  FROM ehrms_tms_timesheet t" +
                    "  WHERE t.TIMESHEET_STATUS != 'Deleted'" +
                    "    AND t.TIMESHEET_DATE BETWEEN :startDate AND :endDate" +
                    ") m " +
                    "JOIN ses_m04_user_association_v a ON m.USER_NAME = a.user_id " +
                    "JOIN emp_employee_master e ON e.EMPLOYEE_ID = a.USER_TYPE_ID " +
                    "LEFT JOIN emp_employee_master rm_master ON rm_master.EMPLOYEE_ID = e.REPORTING_MANAGER_ID " +
                    "WHERE " +
                    "  (:filterVar IS NULL OR :filterVar = '' OR FIND_IN_SET(m.TIMESHEET_STATUS, :filterVar))" +
                    "  AND (:username IS NULL OR :username = '' OR FIND_IN_SET(a.USER_TYPE_ID, :username))" +
                    "  AND (:departments IS NULL OR :departments = '' OR FIND_IN_SET(e.DEPARTMENT_ID, :departments))" +
                    "  AND (:divisions IS NULL OR :divisions = '' OR FIND_IN_SET(e.DIVISION_ID, :divisions)) " +
                    "  AND (:reportingmanagers IS NULL OR :reportingmanagers = '' OR FIND_IN_SET(e.REPORTING_MANAGER_ID, :reportingmanagers))" +
                    " GROUP BY m.user_name, m.week_start" +
                    " ORDER BY m.week_start DESC",
            countQuery = "SELECT COUNT(*) FROM (" +
                    "SELECT m.user_name, " +
                    "  CASE " +
                    "    WHEN DAYNAME(m.TIMESHEET_DATE) = 'Sunday' THEN m.TIMESHEET_DATE " +
                    "    ELSE SUBDATE(m.TIMESHEET_DATE, INTERVAL (WEEKDAY(m.TIMESHEET_DATE) + 1) DAY) " +
                    "  END AS week_start " +
                    "FROM ehrms_tms_timesheet m " +
                    "JOIN ses_m04_user_association_v a ON m.USER_NAME = a.user_id " +
                    "JOIN emp_employee_master e ON e.EMPLOYEE_ID = a.USER_TYPE_ID " +
                    "LEFT JOIN emp_employee_master rm_master ON rm_master.EMPLOYEE_ID = e.REPORTING_MANAGER_ID " +
                    "WHERE " +
                    "  (:filterVar IS NULL OR :filterVar = '' OR FIND_IN_SET(m.TIMESHEET_STATUS, :filterVar)) " +
                    "  AND (:username IS NULL OR :username = '' OR FIND_IN_SET(a.USER_TYPE_ID, :username)) " +
                    "  AND m.TIMESHEET_STATUS != 'Deleted' " +
                    "  AND m.timesheet_date BETWEEN :startDate AND :endDate " +
                    "  AND (:departments IS NULL OR :departments = '' OR FIND_IN_SET(e.DEPARTMENT_ID, :departments)) " +
                    "  AND (:divisions IS NULL OR :divisions = '' OR FIND_IN_SET(e.DIVISION_ID, :divisions)) " +
                    "  AND (:reportingmanagers IS NULL OR :reportingmanagers = '' OR FIND_IN_SET(e.REPORTING_MANAGER_ID, :reportingmanagers)) " +
                    "GROUP BY user_name, week_start" +
                    ") as grouped",
            nativeQuery = true)
    Page<Object[]> findWeeklySummaryByUsernameAndDate(
            @Param("username") String username,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("filterVar") String filterVar,
            @Param("departments") String departments,
            @Param("divisions") String divisions,
            @Param("reportingmanagers") String reportingmanagers,
            Pageable pageable);
}
