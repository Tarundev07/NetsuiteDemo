package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.JobRequisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRequisitionRepository extends JpaRepository<JobRequisition, Integer>, JpaSpecificationExecutor<JobRequisition> {
    Optional<JobRequisition> findById(Integer id);

    Page<JobRequisition> findByDesignation_DesignationNameContainingIgnoreCase(String designationName, Pageable pageable);

    Page<JobRequisition> findByDepartment_DnameContainingIgnoreCase(String dname, Pageable pageable);

    Page<JobRequisition> findByEmployee_FirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    @Query("SELECT j FROM JobRequisition j WHERE j.id NOT IN :ids")
    List<JobRequisition> findJobReqExcludingExisting(@Param("ids") List<Integer> ids);
}
