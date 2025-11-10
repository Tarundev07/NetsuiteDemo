package com.atomicnorth.hrm.tenant.domain.employement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "emp_employee_family")
public class EmployeeFamily extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Integer memberId;

    @Column(name = "USER_NAME")
    private Integer userName;

    @Column(name = "RELATION_CODE", columnDefinition = "VARCHAR(50)")
    private String relationCode;

    @Column(name = "FULL_NAME", columnDefinition = "VARCHAR(100)")
    private String fullName;

    @Column(name = "GENDER_CODE", columnDefinition = "VARCHAR(500)")
    private String genderCode;

    @Column(name = "DOB")
    private Date dob;

    @Column(name = "OCCUPATION_CODE", columnDefinition = "VARCHAR(500)")
    private String occupationCode;

    @Column(name = "IS_DEPENDENT", columnDefinition = "VARCHAR(1)")
    private String isDependent;

    @Column(name = "CONTACT_NUMBER", columnDefinition = "VARCHAR(100)")
    private String contactNumber;

    @Column(name = "REMARK", columnDefinition = "VARCHAR(500)")
    private String remark;

    @Column(name = "IS_ACTIVE", columnDefinition = "VARCHAR(1)")
    private String isActive;
}
