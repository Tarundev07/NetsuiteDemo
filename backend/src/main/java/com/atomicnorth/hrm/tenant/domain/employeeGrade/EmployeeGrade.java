package com.atomicnorth.hrm.tenant.domain.employeeGrade;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.SalaryElementGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name = "ses_m25_employee_grade")
public class EmployeeGrade extends AbstractAuditingEntity<Long> {
    @Id
    @Column(name = "GRADE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "SALARY_STRUCTURED_ID")
    private Integer salaryStructuredId;

    @Column(name = "GRADE_NAME")
    private String gradeName;

    @Column(name = "GRADE_CODE", unique = true)
    private String gradeCode;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SALARY_STRUCTURED_ID", referencedColumnName = "GROUP_ID", insertable = false, updatable = false)
    private SalaryElementGroup salaryElementGroup;

}
