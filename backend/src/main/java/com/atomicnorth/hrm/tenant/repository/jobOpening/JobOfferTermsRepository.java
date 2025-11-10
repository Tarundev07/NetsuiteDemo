package com.atomicnorth.hrm.tenant.repository.jobOpening;

import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOfferTermsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobOfferTermsRepository extends JpaRepository<JobOfferTermsTemplate, Integer> {
}
