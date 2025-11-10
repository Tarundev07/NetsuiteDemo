package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {
    @Query(value = "      SELECT p.PROJECT_ID, p.PROJECT_NAME, p.PROJECT_DESC, p.CREATION_DATE,concat_ws( a.FIRST_NAME,' ',a.LAST_NAME) as USER_NAME,p.PROJECT_TYPE, p.PROJECT_STATUS, p.START_DATE, p.END_DATE,p.PROJECT_RF_NUM FROM ses_m02_project_v p ,ses_m04_user_association m\n" +
            "           JOIN emp_employee_master a ON a.EMPLOYEE_ID = m.USER_TYPE_ID where p.PROJECT_OWNER=a.EMPLOYEE_ID and p.PROJECT_STATUS='Active' order by p.CREATION_DATE desc", nativeQuery = true)
    Page<Map<String, Object>> findByProjectStatus(Pageable pageable);

    @Query(value = "         SELECT distinct p.PROJECT_ID, p.PROJECT_NAME, p.PROJECT_DESC, p.CREATION_DATE,concat_ws(\" \", a.FIRST_NAME,a.LAST_NAME) as USER_NAME,p.PROJECT_TYPE, p.PROJECT_STATUS, p.START_DATE, p.END_DATE,p.PROJECT_RF_NUM FROM ses_m02_project_v p ,ses_m04_user_association m\n" +
            "           JOIN emp_employee_master a ON a.EMPLOYEE_ID = m.USER_TYPE_ID where p.PROJECT_OWNER=a.EMPLOYEE_ID and p.PROJECT_STATUS='Active' and p.PROJECT_RF_NUM in (SELECT PROJECT_RF_NUM FROM ses_m02_project_task_role_user_mapping_v m where m.USER_NAME=:username and (date(now()) between m.START_DATE and m.END_DATE) and m.TASK_RF_NUM is null and m.PROJECT_TASK_ROLE_ID in (3,4,10)) order by p.CREATION_DATE desc", nativeQuery = true)
    Page<Map<String, Object>> findByProjectStatusForUser(String username, Pageable pageable);

    Project findByProjectRfNum(int projectRfNum);

    @Query(value = "           SELECT p.SITE_ID,ca.ACCOUNT_ID,cs.DESCRIPTION as SITE_DESCRIPTION,PROJECT_NAME, PROJECT_DESC, p.CREATION_DATE, p.PROJECT_OWNER, PROJECT_TYPE, PROJECT_STATUS,START_DATE, END_DATE,TIMESHEET_APPROVER,PROJECT_RF_NUM,concat_ws(' ',u.FIRST_NAME,u.LAST_NAME)  as projectOwnerFullname,SCHEDULED_START_DATE,SCHEDULED_END_DATE,ACTUAL_START_DATE,ACTUAL_END_DATE,p.CURRENCY_ID,p.COUNTRY_ID,p.BILLING_HOURS_IN_A_DAY,cs.NAME as SITE_NAME,ca.NAME as CUSTOMER_NAME,ca.DESCRIPTION as CUSTOMER_DESCRIPTION FROM ses_m02_project_v p left join ses_m04_user_association a\n" +
            "           JOIN emp_employee_master u ON u.EMPLOYEE_ID = a.USER_TYPE_ID on p.PROJECT_OWNER=u.EMPLOYEE_ID left join ses_m22_customer_site_v cs on p.SITE_ID=cs.SITE_ID left join ses_m22_customer_account_v ca on cs.ACCOUNT_ID=ca.ACCOUNT_ID where PROJECT_ID=:projectId limit 1", nativeQuery = true)
    List<Map<String, Object>> findProjectDetails(@Param("projectId") String projectId);

    List<Project> findByProjectRfNumIn(List<Integer> projectRfNum);

    Optional<Project> findByProjectName(String projectName);
}