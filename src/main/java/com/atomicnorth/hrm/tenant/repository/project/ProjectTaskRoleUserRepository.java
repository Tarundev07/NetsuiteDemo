package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectTaskRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface ProjectTaskRoleUserRepository extends JpaRepository<ProjectTaskRoleUser, Integer> {

    @Modifying
    @Transactional
    @Query(value = "delete from ses_m02_project_task_role_user_mapping_v where PROJECT_RF_NUM=:projectRfNum and USER_NAME=:username", nativeQuery = true)
    void deleteByProjectIdAndUsername(@Param("projectRfNum") String projectRfNum, @Param("username") String username);

    @Modifying
    @Transactional
    void deleteByTaskRfNumAndUsername(String taskRfNum, String username);

    Optional<ProjectTaskRoleUser> findByUsername(Integer username);
}
