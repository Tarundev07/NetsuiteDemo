package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectTaskAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProjectTaskAllocationRepository extends JpaRepository<ProjectTaskAllocation, Integer> {
    @Query(value = "         SELECT (select GROUP_CONCAT(r.ROLE_NAME SEPARATOR ' | ') from ses_m02_project_task_role_v r where PROJECT_TASK_ROLE_ID in (select PROJECT_TASK_ROLE_ID from ses_m02_project_task_role_user_mapping_v where TASK_RF_NUM=a.TASK_RF_NUM and USER_NAME=a.USER_NAME)) as TASK_ROLES,TASK_ALLOCATION_ID,TASK_RF_NUM,(select TASK_NAME from ses_m02_task_story_v where TASK_RF_NUM=a.TASK_RF_NUM) as TASK_NAME,(select TASK_ID from ses_m02_task_story_v where TASK_RF_NUM=a.TASK_RF_NUM) as TASK_ID, PROJECT_RF_NUM, a.USER_NAME, START_DATE, END_DATE, a.REMARK, IS_DELETED,(select CONCAT_WS(' ',u.FIRST_NAME,u.LAST_NAME) from ses_m04_user_association m\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = m.USER_TYPE_ID WHERE u.EMPLOYEE_ID=a.CREATED_BY) as CREATED_BY,u.EMPLOYEE_NUMBER as USER_CODE,concat_ws(' ',u.FIRST_NAME,u.LAST_NAME) as FULL_NAME,a.DEPUTATION,a.TASK_ASSIGNMENT_NAME,a.ALLOCATION_PERCENTAGE FROM ses_m02_projects_task_allocation_v a,ses_m04_user_association m\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = m.USER_TYPE_ID where a.USER_NAME=u.EMPLOYEE_ID and a.IS_DELETED='N' and TASK_RF_NUM=:taskId order by FULL_NAME", nativeQuery = true)
    List<Map<String, Object>> findUserByTaskId(@Param("taskId") Integer taskId);

    @Query(value = "select count(1) from ses_m02_projects_task_allocation_v where TASK_RF_NUM=:taskRfNum and USER_NAME=:username and IS_DELETED='N' and (:startDate between START_DATE and END_DATE or :endDate between START_DATE and END_DATE)", nativeQuery = true)
    int findProjectTaskAllocation(@Param("taskRfNum") String taskRfNum, @Param("username") String username, @Param("startDate") String startDate, @Param("endDate") String endDate);

    List<ProjectTaskAllocation> findByProjectRfNum(Integer projectId);

    List<ProjectTaskAllocation> findByProjectRfNumIn(List<Integer> projectId);

    List<ProjectTaskAllocation> findByTaskRfNumIn(List<Integer> taskIds);
}
