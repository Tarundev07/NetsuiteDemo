package com.atomicnorth.hrm.tenant.service.dto.employeeGrade;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.employeeGrade.EmployeeGrade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeGradeDTO extends AbstractAuditingEntity<Long> {

    private Integer id;
    @NotNull(message = "Salary structured id is required.")
    private Integer salaryStructuredId;
    @NotNull(message = "Grade name is required.")
    private String gradeName;
    //@NotNull(message = "Grade Code is required.")
    private String gradeCode;
    private String isActive;

    public EmployeeGradeDTO(EmployeeGrade grade) {
        this.id = grade.getId();
        this.salaryStructuredId = grade.getSalaryStructuredId();
        this.gradeCode = grade.getGradeCode();
        this.gradeName = grade.getGradeName();
        this.isActive = grade.getIsActive();
        this.setCreatedBy(grade.getCreatedBy());
        this.setCreatedDate(grade.getCreatedDate());
        this.setLastUpdatedBy(grade.getLastUpdatedBy());
        this.setLastUpdatedDate(grade.getLastUpdatedDate());
    }
}
