package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.TermConditionDepartmentMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermConditionDepartmentMappingRepository extends JpaRepository<TermConditionDepartmentMapping, Integer> {

    List<TermConditionDepartmentMapping> findByTermsConditionId(Integer termsConditionId);
}
