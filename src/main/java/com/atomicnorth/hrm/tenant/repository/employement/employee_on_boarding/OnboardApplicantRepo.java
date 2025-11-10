package com.atomicnorth.hrm.tenant.repository.employement.employee_on_boarding;


import com.atomicnorth.hrm.tenant.domain.employement.employee_on_boarding.OnboardApplicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OnboardApplicantRepo extends JpaRepository<OnboardApplicant, Integer> {

    Optional<OnboardApplicant> findById(Integer onboardingId);

    Page<OnboardApplicant> findAll(Specification<OnboardApplicant> spec, Pageable pageable);

    @Query("SELECT e.jobOffer FROM OnboardApplicant e")
    List<Integer> findAllJobOfferIds();

    Page<OnboardApplicant> findByDesignationEntity_DesignationNameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<OnboardApplicant> findByEmployeeJobApplicant_applicantNameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<OnboardApplicant> findByDepartmentEntity_dnameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<OnboardApplicant> findByHolidaysCalendar_NameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<OnboardApplicant> findByJobOfferEntity_jobOfferNameContainingIgnoreCase(String searchValue, Pageable pageable);

    boolean existsByJobApplicantId(Integer applicantId);


}
