package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ProjectAllocationRepository extends JpaRepository<ProjectAllocation, Integer>, JpaSpecificationExecutor<ProjectAllocation> {
    @Query(value = "    SELECT UNIT_PRICE_PER_HOUR,PROJECT_ALLOCATION_ID,DEPUTATION,a.PROJECT_RF_NUM,(select PROJECT_ID from ses_m02_project_v where PROJECT_RF_NUM=a.PROJECT_RF_NUM) as PROJECT_ID, u.EMPLOYEE_ID, START_DATE, END_DATE, a.REMARK, a.IS_ACTIVE,concat_ws(' ',u.FIRST_NAME,u.LAST_NAME) as FULL_NAME,a.CREATION_DATE,(select CONCAT_WS(' ',u.FIRST_NAME,u.LAST_NAME) from ses_m04_user_association m\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = m.USER_TYPE_ID WHERE u.EMPLOYEE_ID=a.CREATED_BY) as CREATED_BY,(select GROUP_CONCAT(r.ROLE_NAME SEPARATOR '<br>') from ses_m02_project_task_role_v r where PROJECT_TASK_ROLE_ID in (select PROJECT_TASK_ROLE_ID from ses_m02_project_task_role_user_mapping_v where PROJECT_RF_NUM=a.PROJECT_RF_NUM AND u.EMPLOYEE_ID=a.EMPLOYEE_ID)) as PROJECT_ROLES,u.EMPLOYEE_NUMBER as USER_CODE,(select COUNT(DISTINCT(TASK_RF_NUM)) from ses_m02_projects_task_allocation_v f where f.USER_NAME=a.EMPLOYEE_ID and f.TASK_RF_NUM in (select TASK_RF_NUM from ses_m02_task_story_v where PROJECT_RF_NUM=:projectRf)) as USER_TASK_COUNT FROM ses_m02_project_allocation_v a,ses_m04_user_association p\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = p.USER_TYPE_ID where a.EMPLOYEE_ID=u.EMPLOYEE_ID and a.IS_DELETED='N' and a.PROJECT_RF_NUM=:projectRf order by FULL_NAME asc", nativeQuery = true)
    List<Map<String, Object>> findUserByProjectRf(@Param("projectRf") String projectRf);

    @Query(value = "SELECT p.PROJECT_NAME, p.PROJECT_ID AS PROJECT_ID, p.PROJECT_RF_NUM " +
            "FROM ses_m02_project_v p " +
            "WHERE p.PROJECT_RF_NUM IN " +
            "(SELECT DISTINCT(PROJECT_RF_NUM) FROM ses_m02_project_allocation_v " +
            "WHERE EMPLOYEE_ID = :username AND IS_DELETED = 'N') " +
            "ORDER BY p.PROJECT_NAME", nativeQuery = true)
    List<Object[]> getProjectsByUser(@Param("username") String username);

    @Query(value = "select u.EMPLOYEE_ID,concat_ws(\" \", u.FIRST_NAME,u.LAST_NAME) as userfullname, \n" +
            "u.WORK_EMAIL,(case when exists (select EMPLOYEE_ID from ses_m02_project_allocation_v\n" +
            " where PROJECT_RF_NUM=:projectRfNum and EMPLOYEE_ID=u.EMPLOYEE_ID and IS_DELETED='N') then 'Y' else 'N' end)\n" +
            "  as ASSIGN_FLAG,    \n" +
            "           u.EMPLOYEE_NUMBER from  ses_m04_user_association a\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID u where u.POLICY_GROUP='SUPRA-Noida' order by userfullname", nativeQuery = true)
    List<Map<String, Object>> findActiveUsersForProject(@Param("projectRfNum") Integer projectRfNum);


    @Query(value = "select count(1) from ses_m02_project_allocation_v where PROJECT_RF_NUM=:projectRfNum and EMPLOYEE_ID=:username and IS_DELETED='N' and (:startDate between START_DATE and END_DATE or :endDate between START_DATE and END_DATE)", nativeQuery = true)
    int findProjectAllocation(@Param("projectRfNum") String projectRfNum, @Param("username") String username, @Param("startDate") String startDate, @Param("endDate") Date endDate);

    List<ProjectAllocation> findDistinctByEmployeeId(Integer userName);

    List<ProjectAllocation> findAllByProjectRfNum(Integer id);

    Page<ProjectAllocation> findByProject_ProjectIdContainingIgnoreCaseAndProjectRfNum(String projectId, Integer projectRfNum, Pageable pageable);

    Page<ProjectAllocation> findByEmployee_employeeNumberContainingIgnoreCaseAndProjectRfNum(String employeeNumber, Integer projectRfNum, Pageable pageable);

    Page<ProjectAllocation> findByEmployee_firstNameContainingIgnoreCaseAndProjectRfNum(String firstName, Integer projectRfNum, Pageable pageable);

    Page<ProjectAllocation> findByProjectRfNum(Integer projectRfNum, Pageable pageable);

    List<ProjectAllocation> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Integer employeeId, Date endDate, Date startDate);

    List<ProjectAllocation> findByProjectRfNumAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Integer projectRfNum, Date endDate, Date startDate);
}
