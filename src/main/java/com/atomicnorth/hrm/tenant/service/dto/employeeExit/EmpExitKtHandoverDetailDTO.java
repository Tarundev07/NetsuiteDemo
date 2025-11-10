package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmpExitKtHandoverDetailDTO {
    private Integer id;
    private Integer ktHandoverId;
    private Integer projectId;
    private String projectName;
    private Integer ktToEmployeeId;
    private LocalDate ktDate;
    private String ktMode;
    private String ktDocumentPath;
    private String status;
    private String remarks;
    private LocalDate handoverEndDate;
    private String Department;
    private String isDeleted;





}
