package com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WeeklyTimesheetSummary {
    private Integer employeeId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private BigDecimal totalEffort;
    private String employeeName;
    private String reportingManagerName;
    private String status;
    private String weekDates;
    private String approverRemark;
    private String username;
    private Integer timesheetId;
}
