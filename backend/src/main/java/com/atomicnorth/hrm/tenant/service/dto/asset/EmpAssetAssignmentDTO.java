package com.atomicnorth.hrm.tenant.service.dto.asset;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class EmpAssetAssignmentDTO {

    private Integer id;
    private Integer assetId;
    private Integer employeeId;
    private LocalDate assignedDate;
    private LocalDate returnDate;
    private String clearanceStatus;
    private String clearanceAttachment;
    private String remark;
    private String reason;
    private String exitRequestNumber;
    private LocalDate lastWorkingDate;
    private String status;
    private String employeeName;
    private String assetName;
    private String assetCode;
    private String assetClearanceStatus;
}
