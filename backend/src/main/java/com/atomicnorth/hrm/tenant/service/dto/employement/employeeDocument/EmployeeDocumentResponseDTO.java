package com.atomicnorth.hrm.tenant.service.dto.employement.employeeDocument;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeDocumentResponseDTO {

    private Long id;
    private Integer employeeId;
    private String docName;
    private String docNumber;
    private String docType;
    private String remark;
    private String downloadUrl;
    private LocalDateTime creationDate;
    private String createdBy;

    // You can add more fields as needed (like description, isActive, etc.)
}

