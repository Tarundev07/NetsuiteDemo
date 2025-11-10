package com.atomicnorth.hrm.tenant.domain.employement.employee_on_boarding;

import com.atomicnorth.hrm.tenant.domain.Department;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.employeeGrade.EmployeeGrade;
import com.atomicnorth.hrm.tenant.domain.employement.employee_job_applicant.JobApplicant;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendar;
import com.atomicnorth.hrm.tenant.domain.jobOpening.JobOffer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ses_m00_onboard_applicants")
public class OnboardApplicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ONBOARDING_ID")
    private Integer onboardingId;

    @Column(name = "JOB_OFFER_ID")
    private Integer jobOffer;

    @Column(name = "JOB_APPLICANT_ID")
    private Integer jobApplicantId;

    @Column(name = "EMPLOYEE_ONBOARDING_TEMPLATE_ID")
    private Integer employeeOnboardingTemplate;

    @Column(name = "DATE_OF_JOINING")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateOfJoining;

    @Column(name = "DEPARTMENT_ID")
    private Integer department;

    @Column(name = "ONBOARDING_BEGINS_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate onboardingBeginsOn;

    @Column(name = "DESIGNATION_ID")
    private Integer designation;

    @Column(name = "HOLIDAY_LIST_ID")
    private Integer holiday;

    @Column(name = "NOTIFY_USER_BY_EMAIL")
    private String notifyUserByEmail;

    @Column(name = "OPERATION_SOURCE")
    private String operationSource;

    @Column(name = "C_ATTRIBUTE1")
    private String cAttribute1;

    @Column(name = "C_ATTRIBUTE2")
    private String cAttribute2;

    @Column(name = "C_ATTRIBUTE3")
    private String cAttribute3;

    @Column(name = "C_ATTRIBUTE4")
    private String cAttribute4;

    @Column(name = "C_ATTRIBUTE5")
    private String cAttribute5;

    @Column(name = "C_ATTRIBUTE6")
    private String cAttribute6;

    @Column(name = "C_ATTRIBUTE7")
    private String cAttribute7;

    @Column(name = "C_ATTRIBUTE8")
    private String cAttribute8;

    @Column(name = "C_ATTRIBUTE9")
    private String cAttribute9;

    @Column(name = "C_ATTRIBUTE10")
    private String cAttribute10;

    @Column(name = "C_ATTRIBUTE11")
    private String cAttribute11;

    @Column(name = "C_ATTRIBUTE12")
    private String cAttribute12;

    @Column(name = "C_ATTRIBUTE13")
    private String cAttribute13;

    @Column(name = "C_ATTRIBUTE14")
    private String cAttribute14;

    @Column(name = "C_ATTRIBUTE15")
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
    @Temporal(TemporalType.DATE)
    private Date dAttribute26;

    @Column(name = "D_ATTRIBUTE27")
    @Temporal(TemporalType.DATE)
    private Date dAttribute27;

    @Column(name = "D_ATTRIBUTE28")
    @Temporal(TemporalType.DATE)
    private Date dAttribute28;

    @Column(name = "D_ATTRIBUTE29")
    @Temporal(TemporalType.DATE)
    private Date dAttribute29;

    @Column(name = "D_ATTRIBUTE30")
    @Temporal(TemporalType.DATE)
    private Date dAttribute30;

    @Column(name = "J_ATTRIBUTE31")
    private String jAttribute31;

    @Column(name = "J_ATTRIBUTE32")
    private String jAttribute32;

    @Column(name = "CREATION_DATE")
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_DATE")
    @Temporal(TemporalType.DATE)
    private Date lastUpdatedDate;

    @Lob
    @Column(name = "RECORD_INFO")
    private String recordInfo;

    @Transient
    private String designationName;

    @Transient
    private String jobApplicantName;

    @Transient
    private String departmentName;

    @Transient
    private String HolidayName;

    @Transient
    private String JobOfferName;

    @ManyToOne
    @JoinColumn(name = "DESIGNATION_ID", referencedColumnName = "DESIGNATION_ID", insertable = false, updatable = false)
    private Designation designationEntity;

    @ManyToOne
    @JoinColumn(name = "JOB_APPLICANT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private JobApplicant employeeJobApplicant;

    @ManyToOne
    @JoinColumn(name = "DEPARTMENT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private  Department departmentEntity;

    @ManyToOne
    @JoinColumn(name = "HOLIDAY_LIST_ID", referencedColumnName = "HOLIDAY_CALENDAR_ID", insertable = false, updatable = false)
    private HolidaysCalendar holidaysCalendar;

    @ManyToOne
    @JoinColumn(name = "JOB_OFFER_ID", referencedColumnName = "JOB_OFFER_ID", insertable = false, updatable = false)
    private JobOffer jobOfferEntity;

}

