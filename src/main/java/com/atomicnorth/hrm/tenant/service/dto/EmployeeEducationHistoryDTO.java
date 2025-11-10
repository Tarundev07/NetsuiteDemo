package com.atomicnorth.hrm.tenant.service.dto;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeEducationHistoryDTO extends AbstractAuditingEntity<Long> implements Serializable {

    private Integer empEducationId;
    private Integer username;
    private String qualification;
    private String schoolOrCollegeName;
    private String boardOrUniversity;
    private String streamOrDegree;
    private String academicStatus;
    private String yearOfCompletion;
    private String scoreOrCgpa;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
