package com.atomicnorth.hrm.tenant.repository.project;

import com.atomicnorth.hrm.tenant.domain.project.ProjectMilestone;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Integer>, JpaSpecificationExecutor<ProjectMilestone> {
    List<ProjectMilestone> findProjectMilestoneByProjectRfNumAndIsActive(Integer projectRfNum, String status);

    Optional<ProjectMilestone> findByMilestoneNameIgnoreCaseAndProjectRfNum(String milestoneName, Integer projectRfNum);

    Optional<ProjectMilestone> findById(Integer projectMilestoneId);
}
