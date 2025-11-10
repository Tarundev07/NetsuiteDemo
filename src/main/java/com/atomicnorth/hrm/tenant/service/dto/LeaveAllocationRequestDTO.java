package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
@AllArgsConstructor
public class LeaveAllocationRequestDTO implements Serializable {
    private Long id;

    private Integer empId;

    private String empName;

    private Integer totalLeave;

    private String isActive;

    private LeaveAllocationDetailsDTO leaveDetails;
}
