package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.InterviewRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRoundRepository extends JpaRepository<InterviewRound, Long> {

    Optional<InterviewRound> findByInterviewRoundNameIgnoreCase(String interviewRoundName);

    @Query("SELECT s FROM InterviewRound ir JOIN ir.skillsId s WHERE ir.interviewRoundId = :roundId")
    List<Long> findSkillsIdByInterviewRoundId(@Param("roundId") Long roundId);

    @Query(value = "SELECT INTERVIEW_ROUND_NAME FROM ses_m05_interview_round WHERE  INTERVIEW_ROUND_ID=:interviewRoundId ",
            nativeQuery = true)
    Optional<String> findInterviewRoundNameById(@Param("interviewRoundId") Long interviewRoundId);

    @Query(value = "SELECT INTERVIEW_ROUND_ID, INTERVIEW_ROUND_NAME FROM ses_m05_interview_round",nativeQuery = true)
    List<Object[]> findAllInterviewRoundNameAndId();

}
