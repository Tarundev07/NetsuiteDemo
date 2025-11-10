package com.atomicnorth.hrm.tenant.service.dto.leave;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
public class LeaveDetail {

    @NotNull(message = "Leave Type ID cannot be null")
    private Integer leaveTypeId;

    @NotNull(message = "Leave Max Count cannot be null")
    @PositiveOrZero(message = "Leave Max Count must be zero or a positive number")
    private Double leaveMaxCount;

    private String leaveTypeName;

    public LeaveDetail(Integer leaveTypeId, Double leaveMaxCount, String leaveTypeName) {
        this.leaveTypeId = leaveTypeId;
        this.leaveMaxCount = leaveMaxCount;
        this.leaveTypeName = leaveTypeName;
    }


}