package com.atomicnorth.hrm.tenant.domain.jobOpening;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "ses_m00_job_offer_terms_template")
public class JobOfferTermsTemplate extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_OFFER_TEMPLATE_ID")
    private Integer jobOfferTemplateId;
    @Column(name = "OFFER_TERM_MASTER_ID")
    private Integer offerTermMasterId;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "START_DATE")
    private Date startDate;
    @Column(name = "END_DATE")
    private Date endDate;
    @Column(name = "IS_ACTIVE")
    private String isActive;
}
