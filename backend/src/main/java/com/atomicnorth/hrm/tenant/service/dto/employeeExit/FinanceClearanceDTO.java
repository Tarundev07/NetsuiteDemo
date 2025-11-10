package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FinanceClearanceDTO {
    private String exitRequestNumber;
    private Integer id;
    private Integer exitRequestId;
    private Integer employeeId;
    private String employeeName;
    private String  departmentName;
    private LocalDate lastWorkingDay;
    private String status;
    private String finalDocument;
    private Double finalPayable;
    private String financeClearanceStatus;
    private List<FinanceClearanceDetailsDTO> detailsDTOList;
}
