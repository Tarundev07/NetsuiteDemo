package com.atomicnorth.hrm.tenant.service.dto.leave;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    private Long leaveRfNum;

    @NotBlank(message = "Request number is mandatory.")
    private String requestNumber;

    private Double totalDays;
    @NotNull(message = "Employee Id cannot be null.")
    private Integer empId;
    @NotBlank(message = "Leave Type is mandatory.")
    private String leaveCode;
    @NotNull(message = "Start Date is mandatory.")
    private LocalDate startDate;
    private String startOption;
    @NotNull(message = "End Date is mandatory.")
    private LocalDate endDate;
    private String endOption;
    private String purpose;
    private Integer requestedBy;
    private String approveRemark;

    private String userReversalRemark;

    private String status;

    private String leaveYear;

    private String employeeName;
    private String approveFlag;
}
