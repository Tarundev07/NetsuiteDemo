package com.atomicnorth.hrm.tenant.service.dto.reimbursement;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class ExpenseDocumentDTO {

    private Integer docId;
    private Integer requestId;
    private String docName;
    private String docSize;
    private String uploadedby;
    @DateTimeFormat
    private String uploadedon;
    private String docFlag;
    private String doctype;
    private String docLink;
    private byte[] document;
}
