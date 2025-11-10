package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.TermConditionDepartmentMapping;
import com.atomicnorth.hrm.tenant.domain.jobOpening.TermsCondition;
import com.atomicnorth.hrm.tenant.service.dto.jobOpening.TermsConditionDepartmentDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TermsConditionRepository extends JpaRepository<TermsCondition, Integer> {

    @Query(value = "SELECT TERMS_CONDITION_ID, TITLE,IS_ACTIVE FROM ses_m00_terms_condition ", nativeQuery = true)
    List<Object[]> findTermConditionIdAndTitle();

    List<TermsConditionDepartmentDTO> findByTermsConditionId(Integer termsConditionId);
}
