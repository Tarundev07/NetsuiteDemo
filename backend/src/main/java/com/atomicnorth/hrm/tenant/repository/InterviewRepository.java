package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ses_m04_resume (INTERVIEW_ID, JOB_APPLICANT_ID, RESUME) VALUES (:interviewId, :jobApplicantId, :resume) " +
            "ON DUPLICATE KEY UPDATE RESUME = VALUES(RESUME)", nativeQuery = true)
    void saveResume(@Param("interviewId") Long interviewId,
                    @Param("jobApplicantId") Integer jobApplicantId,
                    @Param("resume") byte[] resume);

    @Query(value = "SELECT RESUME FROM ses_m04_resume WHERE INTERVIEW_ID = :interviewId", nativeQuery = true)
    byte[] findResumeByInterviewId(@Param("interviewId") Long interviewId);

    List<Interview> findByJobApplicantId(Integer jobApplicantId);
}
