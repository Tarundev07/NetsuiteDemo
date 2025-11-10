package com.atomicnorth.hrm.tenant.service.dto.employement.employementDocumentDTO;

import lombok.Data;

import java.util.Date;

@Data
public class DocumentResponseDTO {
    private Long docRfNum;
    private String docName;
    private String serverDocName;
    private String description;
    private String remark;
    private String docNumber;
    private String doc;
    private String docType;
    private String userName;
    private String fileUrl;       // URL to download/view the file
    private Date creationDate;
}