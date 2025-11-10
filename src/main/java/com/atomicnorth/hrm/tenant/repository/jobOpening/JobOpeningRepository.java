package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOpening;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOpeningRepository extends JpaRepository<JobOpening, Integer> {
    Page<JobOpening> findAll(Specification<JobOpening> spec, Pageable pageable);

    Page<JobOpening> findByDesignation_DesignationNameContainingIgnoreCase(String designationName, Pageable pageable);

    Page<JobOpening> findByDepartment_DnameContainingIgnoreCase(String dname, Pageable pageable);
}
