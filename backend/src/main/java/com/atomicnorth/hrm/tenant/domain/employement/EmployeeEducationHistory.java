package com.atomicnorth.hrm.tenant.domain.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "emp_employee_edu_hist")
public class EmployeeEducationHistory extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMP_EDUCATION_ID")
    private Integer empEducationId;

    @Column(name = "USER_NAME", nullable = false)
    private Integer userName;

    @Column(name = "QUALIFICATION", columnDefinition = "TEXT")
    private String qualification;

    @Column(name = "SCHOOL_OR_COLLEGE_NAME", columnDefinition = "TEXT")
    private String schoolOrCollegeName;

    @Column(name = "BOARD_OR_UNIVERSITY", columnDefinition = "TEXT")
    private String boardOrUniversity;

    @Column(name = "STREAM_OR_DEGREE", columnDefinition = "TEXT")
    private String streamOrDegree;

    @Column(name = "ACADEMIC_STATUS", length = 50)
    private String academicStatus;

    @Column(name = "YEAR_OF_COMPLETION", columnDefinition = "TEXT")
    private String yearOfCompletion;

    @Column(name = "SCORE_OR_CGPA", columnDefinition = "TEXT")
    private String scoreOrCgpa;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;
}
