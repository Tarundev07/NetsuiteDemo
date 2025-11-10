package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.InterviewFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {

    Optional<InterviewFeedback> findByInterviewIdAndJobApplicantId(@Param("interviewId") Long interviewId, @Param("jobApplicantId") Integer jobApplicantId);

    @Query("SELECT e.jobApplicantId FROM InterviewFeedback e WHERE e.interviewResultCode = :resultCode")
    List<Integer> findAllApplicantByResultCode(@Param("resultCode") String resultCode);

    Optional<InterviewFeedback> findByInterviewId(@Param("interviewId") Long interviewId);

}