package com.atomicnorth.hrm.tenant.repository.attendance;

import com.atomicnorth.hrm.tenant.domain.attendance.SupraShiftDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SupraShiftDetailRepo extends JpaRepository<SupraShiftDetailEntity, Integer> {

    List<SupraShiftDetailEntity> findBySupraShift_ShiftId(Integer shiftId);

    @Query(value = "SELECT SHIFT_ID, CALENDAR_ID, SHIFT_CODE, NAME, DESCRIPTION, IS_DEFAULT, " +
            "(SELECT name FROM ses_m06_attendance_calendar_v WHERE CALENDAR_ID = s.CALENDAR_ID) AS CALENDAR_NAME, " +
            "(SELECT GROUP_CONCAT(CONCAT('', WEEK_DAY, '')) FROM ses_m06_shift_detail " +
            "WHERE SHIFT_ID = s.SHIFT_ID AND WEEKLY_OFF = 'Y') AS WEEK_OFFS " +
            "FROM ses_m06_shift s " +
            "WHERE IS_ACTIVE = 'A' AND CALENDAR_ID IN " +
            "(SELECT CALENDAR_ID FROM ses_m06_attendance_calendar_v " +
            "WHERE CALENDAR_TYPE_ID IN " +
            "(SELECT CALENDAR_TYPE_ID FROM ses_m06_attendance_calendar_type_v " +
            "WHERE POLICY_GROUP = (SELECT u.POLICY_GROUP FROM ses_m04_user_association a\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID WHERE u.EMPLOYEE_ID =:username))) " +
            "AND :date BETWEEN START_DATE AND END_DATE " +
            "ORDER BY CALENDAR_ID, NAME", nativeQuery = true)
    List<Object[]> findShiftDetails(@Param("username") String username, @Param("date") String date);

    @Query(value = " SELECT \n" +
            " SHIFT_ID, \n" +
            " CALENDAR_ID, \n" +
            " SHIFT_CODE, \n" +
            " NAME, \n" +
            " DESCRIPTION, \n" +
            " IS_DEFAULT, \n" +
            " (\n" +
            "SELECT name\n" +
            "FROM ses_m06_attendance_calendar_v\n" +
            "WHERE CALENDAR_ID = s.CALENDAR_ID) AS CALENDAR_NAME, \n" +
            " (\n" +
            "SELECT GROUP_CONCAT(WEEK_DAY)\n" +
            "FROM ses_m06_shift_detail\n" +
            "WHERE SHIFT_ID = s.SHIFT_ID AND WEEKLY_OFF = 'Y'\n" +
            ") AS WEEK_OFFS\n" +
            "FROM \n" +
            " ses_m06_shift s\n" +
            "WHERE \n" +
            " IS_ACTIVE = 'A' AND CALENDAR_ID IN (\n" +
            "SELECT CALENDAR_ID\n" +
            "FROM ses_m06_attendance_calendar_v\n" +
            "WHERE CALENDAR_TYPE_ID IN (\n" +
            "SELECT CALENDAR_TYPE_ID\n" +
            "FROM ses_m06_attendance_calendar_type_v\n" +
            "WHERE POLICY_GROUP IN (SELECT DISTINCT u.POLICY_GROUP\n" +
            "            FROM ses_m04_user_association a\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID\n" +
            "            WHERE u.EMPLOYEE_ID IN (:username)" +
            ")\n" +
            ") AND :date BETWEEN START_DATE AND END_DATE\n" +
            "ORDER BY \n" +
            " CALENDAR_ID, \n" +
            " NAME;", nativeQuery = true)
    List<Object[]> findShiftDetail(@Param("username") List<Object[]> username, @Param("date") String date);

    @Query(value = "SELECT SHIFT_ID, CALENDAR_ID, SHIFT_CODE, NAME, DESCRIPTION, IS_DEFAULT, " +
            "(SELECT GROUP_CONCAT(CONCAT('', WEEK_DAY, '')) FROM ses_m06_shift_detail " +
            "WHERE SHIFT_ID = s.SHIFT_ID AND WEEKLY_OFF = 'Y') AS WEEK_OFFS " +
            "FROM ses_m06_shift s " +
            "WHERE IS_ACTIVE = 'A' " +
            "AND :date BETWEEN START_DATE AND END_DATE " +
            "ORDER BY  NAME", nativeQuery = true)
    List<Object[]> activeShiftsBasedOnCurrDate(@Param("date") String date);

    List<SupraShiftDetailEntity> findBySupraShift_ShiftIdIn(Set<Integer> shiftId);
}
