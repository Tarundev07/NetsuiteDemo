package com.atomicnorth.hrm.tenant.domain.employement.employee_job_applicant;

import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOpening;
import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ses_m00_employee_job_applicant")
@Data
public class JobApplicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "APPLICANT_NAME", length = 255)
    private String applicantName;

    @Column(name = "JOB_OPENING_ID")
    private Integer jobOpeningId;

    @Column(name = "EMAIL_ADDRESS", length = 255)
    private String emailAddress;

    @Column(name = "DESIGNATION_ID")
    private Integer designationId;

    @Column(name = "DEPARTMENT_ID")
    private Integer departmentId;

    @Column(name = "COUNTRY_CODE")
    private String contactCountryCode;

    @Column(name = "PHONE_NUMBER", length = 10)
    private String phoneNumber;

    @Column(name = "STATUS", length = 8)
    private String status;

    @Column(name = "COUNTRY", length = 255)
    private Integer country;

    @Column(name = "SOURCE", length = 255)
    private String source;

    @Column(name = "SOURCE_DETAILS", length = 255)
    private String sourceDetail;

    @Column(name = "APPLICANT_RATING")
    private Integer applicantRating;

    @Lob
    @Column(name = "COVER_LETTER")
    private String coverLetter;

    //@Lob
    @Column(name = "RESUME_ATTACHMENT")
    private String resumeAttachment;

    @Column(name = "RESUME_LINK", length = 255)
    private String resumeLink;

    @Column(name = "CURRENCY", length = 50)
    private String currency;

    @Column(name = "LOWER_RANGE", precision = 10, scale = 2)
    private BigDecimal lowerRange;

    @Column(name = "UPPER_RANGE", precision = 10, scale = 2)
    private BigDecimal upperRange;

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

    @Temporal(TemporalType.DATE)
    @Column(name = "D_ATTRIBUTE26")
    private Date dAttribute26;

    @Temporal(TemporalType.DATE)
    @Column(name = "D_ATTRIBUTE27")
    private Date dAttribute27;

    @Temporal(TemporalType.DATE)
    @Column(name = "D_ATTRIBUTE28")
    private Date dAttribute28;

    @Temporal(TemporalType.DATE)
    @Column(name = "D_ATTRIBUTE29")
    private Date dAttribute29;

    @Temporal(TemporalType.DATE)
    @Column(name = "D_ATTRIBUTE30")
    private Date dAttribute30;

    @Column(name = "J_ATTRIBUTE31", length = 50)
    private String jAttribute31;

    @Column(name = "J_ATTRIBUTE32", length = 50)
    private String jAttribute32;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATION_DATE")
    private Date creationDate;

    @Column(name = "CREATED_BY", length = 240)
    private String createdBy;

    @Column(name = "LAST_UPDATED_BY", length = 240)
    private String lastUpdatedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    @Lob
    @Column(name = "RECORD_INFO")
    private String recordInfo;

    @Transient
    private String designationName;

    @Transient
    private String jobOpeningName;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "DESIGNATION_ID", referencedColumnName = "DESIGNATION_ID", insertable = false, updatable = false)
    private Designation designationEntity;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "JOB_OPENING_ID", referencedColumnName = "JOB_OPENING_ID", insertable = false, updatable = false)
    private JobOpening jobOpening;
}

