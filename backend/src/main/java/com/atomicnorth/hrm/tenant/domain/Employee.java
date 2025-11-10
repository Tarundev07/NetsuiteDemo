package com.atomicnorth.hrm.tenant.domain;


import com.atomicnorth.hrm.tenant.domain.accessgroup.SesM00UserDivisionMaster;
import com.atomicnorth.hrm.tenant.domain.attendance.AttendanceMoaf;
import com.atomicnorth.hrm.tenant.domain.designation.Designation;
import com.atomicnorth.hrm.tenant.domain.project.ProjectAllocationHistory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "emp_employee_master")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "EMPLOYEE_NUMBER", length = 50, nullable = false, unique = true)
    private String employeeNumber;  // list

    @Column(name = "SALUTATION", nullable = false, length = 10)
    private String salutation;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName; //list

    @Column(name = "MIDDLE_NAME", length = 100)
    private String middleName;

    @Column(name = "LAST_NAME")
    private String lastName; //list

    @Column(name = "DISPLAY_NAME", length = 100)
    private String displayName; //list

    @Column(name = "DOB", nullable = false)
    private LocalDate dob;

    @Column(name = "GENDER_CODE", nullable = false, length = 50)
    private String genderCode;

    @Column(name = "DIVISION_ID", nullable = false)
    private Integer divisionId; // Id or Primary Key

    @Column(name = "DEPARTMENT_ID", nullable = false)
    private Long departmentId; //name list

    @Column(name = "DESIGNATION_ID")
    private Integer designationId;

    @Column(name = "REPORTING_MANAGER_ID")
    private Integer reportingManagerId; //name list

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORTING_MANAGER_ID", referencedColumnName = "EMPLOYEE_ID", insertable = false, updatable = false)
    private Employee reportingManager;

    @OneToMany(mappedBy = "employee" , fetch = FetchType.LAZY)
    private Set<ProjectAllocationHistory> allocations;

    @Column(name = "HR_MANAGER_ID")
    private Integer hrManagerId; // name list

    @Column(name = "IS_VERIFIED", length = 1)
    private String isVerified;

    @Column(name = "NATIONALITY", nullable = false, length = 50)
    private Integer nationality;

    @Column(name = "IS_ACTIVE", length = 50)
    private String isActive;  //list

    @Column(name = "EMPLOYEE_TYPE", length = 50)
    private String employeeType; //list

    @Column(name = "EMPLOYMENT_TYPE_CODE", length = 50)
    private String employmentType; //list

    @Column(name = "EFFECTIVE_START_DATE")
    private LocalDate effectiveStartDate; //list

    @Column(name = "EFFECTIVE_END_DATE")
    private LocalDate effectiveEndDate; //list

    @Column(name = "UNIQUE_IDENTIFIER", length = 50)
    private String uniqueIdentifier;

    @Column(name = "PAN", length = 50, unique = true, nullable = false)
    private String panNumber;

    @Column(name = "AADHAR_NUMBER", unique = true, length = 50, nullable = false)
    private String aadhaarNumber;

    @Column(name = "PASSPORT_NUMBER", length = 50)
    private String passportNumber;

    @Column(name = "DL_NUMBER", length = 50)
    private String dlNumber;

    @Column(name = "BUSINESS_GROUP_ID", nullable = false, length = 50)
    private String businessGroupId;

    @Column(name = "POLICY_GROUP", nullable = false, length = 50)
    private String policyGroup;

    @Column(name = "MOTHER_TONGUE", length = 50)
    private String motherTongue;

    @Column(name = "MARITAL_STATUS", length = 50, nullable = false)
    private String maritalStatus;

    @Column(name = "MARRIAGE_DATE")
    private LocalDate marriageDate;

    @Column(name = "SPOUSE_NAME", length = 50)
    private String spouseName;

    @Column(name = "FATHER_NAME", length = 100)
    private String fatherName;

    @Column(name = "MOTHER_NAME", length = 100)
    private String motherName;

    @Column(name = "PERSONAL_EMAIL", length = 100)
    private String personalEmail;  //list

    @Column(name = "WORK_EMAIL", length = 100)
    private String workEmail;

    @Column(name = "PRIMARY_CONTACT_NUMBER", length = 100)
    private String primaryContactNumber; //list

    @Column(name = "PRIMARY_CONTACT_COUNTRY_CODE", length = 5)
    private String primaryContactCountryCode;

    @Column(name = "SECONDARY_CONTACT_NUMBER", length = 100)
    private String secondaryContactNumber;

    @Column(name = "SECONDARY_CONTACT_COUNTRY_CODE", length = 5)
    private String secondaryContactCountryCode;

    @Column(name = "PRIMARY_EMERGENCY_CONTACT_NAME", length = 100)
    private String primaryEmergencyContactName;

    @Column(name = "PRIMARY_EMERGENCY_CONTACT_RELATION", length = 100)
    private String primaryEmergencyContactRelation;

    @Column(name = "PRIMARY_EMERGENCY_CONTACT_NUMBER", length = 100)
    private String primaryEmergencyContactNumber;

    @Column(name = "PRIMARY_EMERGENCY_CONTACT_COUNTRY_CODE", length = 5)
    private String primaryEmergencyContactCountryCode;

    @Column(name = "SECONDARY_EMERGENCY_CONTACT_NAME", length = 100)
    private String secondaryEmergencyContactName;

    @Column(name = "SECONDARY_EMERGENCY_CONTACT_RELATION", length = 100)
    private String secondaryEmergencyContactRelation;

    @Column(name = "SECONDARY_EMERGENCY_CONTACT_NUMBER", length = 100)
    private String secondaryEmergencyContactNumber;

    @Column(name = "SECONDARY_EMERGENCY_CONTACT_COUNTRY_CODE", length = 5)
    private String secondaryEmergencyContactCountryCode;

    @Column(name = "WEBSITE_URL", length = 100)
    private String websiteUrl;

    @Column(name = "LINKEDIN_URL", length = 100)
    private String linkedinUrl;

    @Column(name = "BIRTH_PLACE", length = 200)
    private String birthPlace;

    @Column(name = "ON_BOARDING_DATE")
    private LocalDate onBoardingDate;  //list

    @Column(name = "OFF_BOARDING_DATE")
    private LocalDate offBoardingDate;  //list

    @Column(name = "BASE_LOCATION", columnDefinition = "TEXT")
    private String baseLocation;

    /*MEDICAL INFO*/
    @Column(name = "BLOOD_GROUP", columnDefinition = "TEXT")
    private String bloodGroup;

    @Column(name = "FITNESS_LEVEL", columnDefinition = "TEXT")
    private String fitnessLevel;

    @Column(name = "DISABILITY", columnDefinition = "TEXT")
    private String disability;

    @Column(name = "DISABILITY_PERCENTAGE", columnDefinition = "TEXT")
    private String disabilityPercentage;

    @Column(name = "DESCRIPTION_MEDICAL_INFO", columnDefinition = "TEXT")
    private String descriptionMedicalInfo;

    @Column(name = "RETIREMENT_DATE")
    private LocalDate retirementDate;
    @Column(name = "CONFIRMATION_DATE")
    private LocalDate confirmationDate;
    @Column(name = "NOTICE_DAYS")
    private Integer noticeDays;//list

    @Column(name = "PAYROLL_CYCLE")
    private Integer payrollCycle;

    @Column(name = "PAYROLL_COST_CENTER_CODE", nullable = false, length = 100)
    private String payrollCostCenterCode;  //lookup done
    @Column(name = "CTC")
    private Long ctc;
    @Column(name = "CURRENCY_CODE", nullable = false)
    private String currencyCode; //lookup Done
    @Column(name = "SALARY_MODE_CODE", nullable = false)
    private String salaryModeCode; // lookup done
    @Column(name = "PF_NUM")
    private String pfNum;
    @Column(name = "DATE_OF_ISSUE_PASSPORT")
    private LocalDate dateOfIssuePassport;
    @Column(name = "PLACE_OF_ISSUE_PASSPORT")
    private String placeOfIssuePassport;
    @Column(name = "VALID_UPTO_PASSPORT")
    private LocalDate validUpToPassport;
    @Column(name = "RESIGNATION_DATE")
    private LocalDate resignationDate;

    @Column(name = "EMP_GRADE_ID")
    private String empGradeId; //list
    @Column(name = "EMP_BRANCH")
    private String empBranch; //list
    @Column(name = "HOLIDAY_LIST_ID", nullable = false)
    private Integer holidayListId;//pending
    @Column(name = "DEFAULT_SHIFT_ID", nullable = false)
    private Integer defaultShiftId;// list
    @Column(name = "JOB_APPLICANT_ID", nullable = false)
    private Integer jobApplicantId;
    @Column(name = "OFFER_DATE")
    private LocalDate offerDate;


    @Column(name = "CREATION_DATE")
    private LocalDate creationDate;

    @Column(name = "CREATED_BY", length = 30)
    private String createdBy; 

    @Column(name = "LAST_UPDATED_BY", length = 50)
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_DATE")
    private LocalDate lastUpdateDate;

    @Transient
    private String departmentName;
    @Transient
    private String designationName;
    @Transient
    private String divisionName;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private Department department;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESIGNATION_ID", referencedColumnName = "DESIGNATION_ID", insertable = false, updatable = false)
    private Designation designation;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DIVISION_ID", referencedColumnName = "DIVISION_ID", insertable = false, updatable = false)
    private SesM00UserDivisionMaster divisionMaster;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID", insertable = false, updatable = false)
    private List<AttendanceMoaf> attendanceMoafList;

    public String getFullName() {
        return Stream.of(firstName, middleName, lastName)
                .filter(Objects::nonNull)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(" "));
    }
}

