package com.atomicnorth.hrm.tenant.service.dto.project.ProjectResponse;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectDocumentResponseDTO {
  /*  private Integer docRfNum;
    private String docName;
    private String description;
    private String remark;
    private String docNumber;
    private String docType;
    private String projectRfNum;
    private String documentDownloadLink;*/

    private Integer id;
    private String projectRfNum;
    private String docName;
    private String docNumber;
    private String docType;
    private String remark;
    private String isActive;
    private String downloadUrl;
    private LocalDateTime creationDate;
    private String createdBy;

    // getters & setters
}

