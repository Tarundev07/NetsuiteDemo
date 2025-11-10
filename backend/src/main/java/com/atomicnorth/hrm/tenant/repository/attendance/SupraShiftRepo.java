package com.atomicnorth.hrm.tenant.repository.attendance;

import com.atomicnorth.hrm.tenant.domain.attendance.SupraShiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;


public interface SupraShiftRepo extends JpaRepository<SupraShiftEntity, Integer>, JpaSpecificationExecutor<SupraShiftEntity> {
    @Query(value = "SELECT e.SHIFT_EMP_ID, e.SHIFT_ID, e.EMPLOYEE_ID, e.START_DATE, e.END_DATE, e.IS_ACTIVE, " +
            "(SELECT name FROM ses_m06_shift WHERE shift_id = e.shift_id) AS NAME, " +
            "(SELECT GENERAL_START_TIME FROM ses_m06_shift WHERE shift_id = e.shift_id) AS GENERAL_START_TIME, " +
            "(SELECT GENERAL_END_TIME FROM ses_m06_shift WHERE shift_id = e.shift_id) AS GENERAL_END_TIME, " +
            "(SELECT DATE_CHANGE_FLAG FROM ses_m06_shift WHERE shift_id = e.shift_id) AS DATE_CHANGE_FLAG, " +
            "(SELECT EMP_ID FROM ses_m25_emp_biometric_mapping WHERE USER_NAME = e.EMPLOYEE_ID) AS USER_BIOMETRIC_ID " +
            "FROM ses_m07_shift_emp e " +
            "WHERE e.IS_ACTIVE = 'Y' AND e.EMPLOYEE_ID = :username", nativeQuery = true)
    List<Map<String, Object>> findShiftDetailsByUsername(@Param("username") String username);

    @Query(value = "SELECT e.SHIFT_EMP_ID, e.SHIFT_ID, e.EMPLOYEE_ID, e.START_DATE, e.END_DATE, e.IS_ACTIVE, \n" +
            "\t\t\t(select CALENDAR_ID from ses_m06_shift where SHIFT_ID=e.SHIFT_ID) as CALENDAR_ID FROM ses_m07_shift_emp e \n" +
            "\t\t\t\t\twhere e.IS_ACTIVE='Y' and e.EMPLOYEE_ID=:username and (START_DATE >=:firstDay  AND END_DATE <= :lastDay)\n" +
            "             order by e.START_DATE, e.END_DATE;", nativeQuery = true)
    List<Map<String, Object>> findShiftsByUsernameAndDateRange(@Param("username") String username,
                                                               @Param("firstDay") String firstDay,
                                                               @Param("lastDay") String lastDay);

    @Query(value = "select SHIFT_ID,SHIFT_CODE from ses_m06_shift ", nativeQuery = true)
    List<Map<String, Object>> findShiftIdandShiftCode();

    @Modifying
    @Transactional
    @Query(value = "update ses_m07_emp_roster_remark set TEMP_LEAVE_FLAG='N' where USER_NAME=:username and DATE=:format", nativeQuery = true)
    void updateFlag(@Param("username") String username,
                    @Param("format") String format);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ses_m07_shift_emp (SHIFT_ID, EMPLOYEE_ID, START_DATE, END_DATE, IS_ACTIVE, LAST_UPDATED_BY, LAST_UPDATED_DATE, CREATED_BY, CREATION_DATE) " +
            "VALUES (:shiftId, :username, :shiftStartDate, :shiftEndDate, :isActive, :lastUpdatedBy, :lastUpdateDate, :createdBy, :creationDate)", nativeQuery = true)
    void insertShiftEmp(@Param("shiftId") String shiftId,
                        @Param("username") String username,
                        @Param("shiftStartDate") String shiftStartDate,
                        @Param("shiftEndDate") String shiftEndDate,
                        @Param("isActive") String isActive,
                        @Param("lastUpdatedBy") String lastUpdatedBy,
                        @Param("lastUpdateDate") String lastUpdateDate,
                        @Param("createdBy") String createdBy,
                        @Param("creationDate") String creationDate);

    @Query(value = " select se.SHIFT_EMP_ID as shiftEmpId,se.SHIFT_ID as shiftId,s.SHIFT_CODE as shiftCode, se.START_DATE as shiftStartDate, se.END_DATE as shiftEndDate, s.COLOR_CODE as colorCode from ses_m07_shift_emp se,ses_m06_shift s where se.SHIFT_ID=s.SHIFT_ID and EMPLOYEE_ID=:userName  \n" +
            " AND se.IS_ACTIVE = 'Y' AND (se.END_DATE BETWEEN DATE :firstDay AND DATE :lastDay ) \n" +
            " order by se.START_DATE asc ", nativeQuery = true)
    List<Map<String, Object>> fetchShieftDetails(@Param("userName") String userName, @Param("firstDay") String firstDay, @Param("lastDay") String lastDay);

    @Query(value = "select SHIFT_ID,SHIFT_CODE from ses_m06_shift ", nativeQuery = true)
    List<Map<String, Object>> getShiftIdAndShiftCode();

    @Query(value = "      SELECT u.EMPLOYEE_ID from  ses_m04_user_association a\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID  WHERE u.EMPLOYEE_NUMBER=:usercode limit 1", nativeQuery = true)
    String findUsernameByUsercode(@Param("usercode") String usercode);

    List<SupraShiftEntity> findByIsActiveOrderByShiftCode(String isActive);
}
