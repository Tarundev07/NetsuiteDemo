package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Integer>, JpaSpecificationExecutor<JobOffer> {
    Optional<JobOffer> findByJobApplicantId(Integer jobApplicantId);
    Page<JobOffer> findByDesignation_DesignationNameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<JobOffer> findByTermsCondition_titleContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<JobOffer> findByEmployeeJobApplicant_applicantNameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<JobOffer> findByJobOfferTermsTemplate_titleContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<JobOffer> findAll(Specification<JobOffer> spec, Pageable pageable);

    @Query("SELECT jo FROM JobOffer jo WHERE jo.id NOT IN :ids")
    List<JobOffer> findJobOffersExcludingExisting(@Param("ids") List<Integer> ids);

    @Query("SELECT e.jobApplicantId FROM JobOffer e")
    List<Integer> findAllApplicantWithOffer();

}
