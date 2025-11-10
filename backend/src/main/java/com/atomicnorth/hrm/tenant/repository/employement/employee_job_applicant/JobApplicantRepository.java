package com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant;


import com.atomicnorth.hrm.tenant.domain.employement.employee_job_applicant.JobApplicant;
import com.atomicnorth.hrm.tenant.domain.project.ProjectAllocation;
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
public interface JobApplicantRepository extends JpaRepository<JobApplicant, Integer>, JpaSpecificationExecutor<JobApplicant> {

    Optional<JobApplicant> findById(Integer id);

    @Query(value = "SELECT JOB_OPENING_ID, JOB_TITLE, DESIGNATION_ID, STATUS, POSTED_ON " +
            "FROM ses_m00_job_opening WHERE  JOB_OPENING_ID=:job_opening_id ",
            nativeQuery = true)
    List<Object[]> findCustomJobOpeningsById(@Param("job_opening_id") Integer JOB_OPENING_ID);

    @Query(value = "SELECT JOB_OPENING_ID, JOB_TITLE, DESIGNATION_ID, STATUS, POSTED_ON " +
            "FROM ses_m00_job_opening ",
            nativeQuery = true)
    List<Object[]> findCustomJobOpenings();

    @Query(value = "SELECT DESIGNATION_ID, DESIGNATION_NAME, DESCRIPTION, START_DATE, END_DATE " +
            "FROM ses_m00_designation WHERE  DESIGNATION_ID=:designation_id ",
            nativeQuery = true)
    List<Object[]> findCustomJobDesignationById(@Param("designation_id") Integer DESIGNATION_ID);

    @Query(value = "SELECT DESIGNATION_NAME FROM ses_m00_designation WHERE  DESIGNATION_ID=:designationId ",
            nativeQuery = true)
    Optional<String> findCustomJobDesignationNameById(@Param("designationId") Long designationId);

    @Query(value = "SELECT APPLICANT_NAME FROM ses_m00_employee_job_applicant WHERE  ID=:id ",
            nativeQuery = true)
    Optional<String> findJobApplicantNameById(@Param("id") Integer id);

    @Query(value = "SELECT ID, APPLICANT_NAME, EMAIL_ADDRESS, DESIGNATION_ID, PHONE_NUMBER,COUNTRY, DEPARTMENT_ID, RESUME_ATTACHMENT " +
            "FROM ses_m00_employee_job_applicant WHERE ID NOT IN (:existingIds)",
            nativeQuery = true)
    List<Object[]> findApplicantDetailsExcludingExisting(@Param("existingIds") List<Integer> existingIds);

    @Query(value = "SELECT ID, APPLICANT_NAME, EMAIL_ADDRESS, DESIGNATION_ID, PHONE_NUMBER,COUNTRY, DEPARTMENT_ID, RESUME_ATTACHMENT " +
            "FROM ses_m00_employee_job_applicant WHERE ID IN (:existingIds)",
            nativeQuery = true)
    List<Object[]> findApplicantDetails(@Param("existingIds") List<Integer> existingIds);

    Page<JobApplicant> findByDesignationEntity_DesignationNameContainingIgnoreCase(String searchValue, Pageable pageable);

    Page<JobApplicant> findByJobOpening_JobTitleContainingIgnoreCase(String searchValue, Pageable pageable);

    Optional<JobApplicant> findByEmailAddress(String emailAddress);

    Optional<JobApplicant> findByPhoneNumber(String phoneNumber);

}

