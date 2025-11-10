package com.atomicnorth.hrm.tenant.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
@AllArgsConstructor
public class LeaveAllocationDetailsDTO implements Serializable {

    private Long id;

    private Long leaveAllocationId;

    private String leaveCode;

    private String leaveName;

    private Double leaveAllocationNumber;

    private Double leaveBalance;

    private Boolean carryForward;

    private Date effectiveStartDate;

    private Date effectiveEndDate;

    private String isActive = "A";
    private String remarks;
    private String allocationType;
}
