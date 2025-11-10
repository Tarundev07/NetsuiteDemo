package com.atomicnorth.hrm.tenant.domain.jobOpening;

import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.employement.employee_job_applicant.JobApplicant;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "ses_m00_job_offer")
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_OFFER_ID")
    private Integer jobOfferId;
    @Column(name = "JOB_OFFER_NAME")
    private String jobOfferName;
    @Column(name = "JOB_APPLICANT_ID", unique = true)
    private Integer jobApplicantId;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "OFFER_DATE")

//    @Temporal(TemporalType.DATE)
    private LocalDate offerDate;

    @Column(name = "NOTICE_PERIOD")
    private Integer noticePeriod;

    @Column(name = "DESIGNATION_ID", unique = true)
    private Integer designationId;
    @Column(name = "JOB_OFFER_TEMPLATE_ID")
    private Integer jobOfferTemplateId;

    @Temporal(TemporalType.DATE)
    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "TERMS_CONDITION_ID", unique = true)
    private Integer termsConditionId;

    @Temporal(TemporalType.DATE)
    @Column(name = "END_DATE")
    private Date endDate;
    @JsonManagedReference
    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<JobOfferTerm> jobOfferTermTemplates;

    @Column(name = "C_ATTRIBUTE1", length = 240)
    private String cAttribute1;

    @Column(name = "C_ATTRIBUTE2", length = 240)
    private String cAttribute2;

    @Column(name = "C_ATTRIBUTE3", length = 240)
    private String cAttribute3;

    @Column(name = "C_ATTRIBUTE4", length = 240)
    private String cAttribute4;

    @Column(name = "C_ATTRIBUTE5", length = 240)
    private String cAttribute5;

    @Column(name = "C_ATTRIBUTE6", length = 240)
    private String cAttribute6;

    @Column(name = "C_ATTRIBUTE7", length = 240)
    private String cAttribute7;

    @Column(name = "C_ATTRIBUTE8", length = 240)
    private String cAttribute8;

    @Column(name = "C_ATTRIBUTE9", length = 240)
    private String cAttribute9;

    @Column(name = "C_ATTRIBUTE10", length = 240)
    private String cAttribute10;

    @Column(name = "C_ATTRIBUTE11", length = 240)
    private String cAttribute11;

    @Column(name = "C_ATTRIBUTE12", length = 240)
    private String cAttribute12;

    @Column(name = "C_ATTRIBUTE13", length = 240)
    private String cAttribute13;

    @Column(name = "C_ATTRIBUTE14", length = 240)
    private String cAttribute14;

    @Column(name = "C_ATTRIBUTE15", length = 240)
    private String cAttribute15;

    @Column(name = "N_ATTRIBUTE16")
    private Integer nAttribute16;

    @Column(name = "N_ATTRIBUTE17")
    private Integer nAttribute17;

    @Column(name = "N_ATTRIBUTE18")
    private Integer nAttribute18;

    @Column(name = "N_ATTRIBUTE19")
    private Integer nAttribute19;

    @Column(name = "N_ATTRIBUTE20")
    private Integer nAttribute20;

    @Column(name = "N_ATTRIBUTE21")
    private Integer nAttribute21;

    @Column(name = "N_ATTRIBUTE22")
    private Integer nAttribute22;

    @Column(name = "N_ATTRIBUTE23")
    private Integer nAttribute23;

    @Column(name = "N_ATTRIBUTE24")
    private Integer nAttribute24;

    @Column(name = "N_ATTRIBUTE25")
    private Integer nAttribute25;

    @Column(name = "D_ATTRIBUTE26")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute26;

    @Column(name = "D_ATTRIBUTE27")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute27;

    @Column(name = "D_ATTRIBUTE28")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute28;

    @Column(name = "D_ATTRIBUTE29")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute29;

    @Column(name = "D_ATTRIBUTE30")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dAttribute30;

    @Column(name = "J_ATTRIBUTE31", length = 50)
    private String jAttribute31;

    @Column(name = "J_ATTRIBUTE32", length = 50)
    private String jAttribute32;
    @Column(name = "CREATION_DATE")
    private Date creationDate;

    @Column(name = "CREATED_BY", length = 240)
    private String createdBy;

    @Column(name = "LAST_UPDATED_BY", length = 240)
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    @Column(name = "RECORD_INFO", columnDefinition = "TEXT")
    private String recordInfo;

    @Transient
    private String designationName;

    @Transient
    private String termAndConditionName;

    @Transient
    private String jobApplicantName;

    @Transient
    private String jobOfferTemplateName;

    @ManyToOne
    @JoinColumn(name = "DESIGNATION_ID", referencedColumnName = "DESIGNATION_ID", insertable = false, updatable = false)
    private Designation designation;

    @ManyToOne
    @JoinColumn(name = "TERMS_CONDITION_ID", referencedColumnName = "TERMS_CONDITION_ID", insertable = false, updatable = false)
    private TermsCondition termsCondition;

    @ManyToOne
    @JoinColumn(name = "JOB_APPLICANT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private JobApplicant employeeJobApplicant;

    @ManyToOne
    @JoinColumn(name = "JOB_OFFER_TEMPLATE_ID", referencedColumnName = "JOB_OFFER_TEMPLATE_ID", insertable = false, updatable = false)
    private JobOfferTermsTemplate jobOfferTermsTemplate;
}

