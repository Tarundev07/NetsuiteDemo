package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.InterviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewTypeRepository extends JpaRepository<InterviewType, Long>, JpaSpecificationExecutor<InterviewType> {

    Optional<InterviewType> findByNameIgnoreCase(String name);

    @Query(value = "SELECT INTERVIEW_TYPE_NAME FROM ses_m04_master_interview_type WHERE  INTERVIEW_TYPE_ID=:interviewTypeId ",
            nativeQuery = true)
    Optional<String> findInterviewTypeNameById(@Param("interviewTypeId") Long interviewTypeId);

    @Query(value = "SELECT INTERVIEW_TYPE_ID, INTERVIEW_TYPE_NAME FROM ses_m04_master_interview_type",nativeQuery = true)
    List<Object[]> findAllInterviewTypeNameAndId();
}
