package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectTaskRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProjectTaskRoleRepository extends JpaRepository<ProjectTaskRole, Integer> {
    @Query(value = "select PROJECT_TASK_ROLE_ID,ROLE_NAME,CASE when exists (select 1 from ses_m02_project_task_role_user_mapping_v where USER_NAME=:username and PROJECT_RF_NUM=:projectRfNum and PROJECT_TASK_ROLE_ID=r.PROJECT_TASK_ROLE_ID) then 'Y' else 'N' end as STATUS from ses_m02_project_task_role_v r where ROLE_LEVEL='Project' order by ROLE_NAME", nativeQuery = true)
    List<Map<String, Object>> getProjectRole(@Param("projectRfNum") String projectRfNum, @Param("username") String username);
}
