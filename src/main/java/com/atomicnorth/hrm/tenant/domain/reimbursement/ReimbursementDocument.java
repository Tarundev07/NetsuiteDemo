package com.atomicnorth.hrm.tenant.domain.reimbursement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "ses_m00_rms_document")
public class ReimbursementDocument extends AbstractAuditingEntity<Long> {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DOC_ID")
    private Integer docid;

    @Column(name = "REQUEST_NUMBER")
    private String requestNumber;
    @Column(name = "REQUEST_ID")
    private Integer requestId;
    @Column(name = "DOC_CAPTION")
    private String doccaption;

    @Column(name = "DOC_NAME")
    private String docname;

    @Column(name = "DOC_SIZE")
    private String docsize;

    @Column(name = "EXPENSE_CODE")
    private String expensecode;

    @Column(name = "UPLOADED_BY")
    private String uploadedby;

    @Column(name = "UPLOADED_ON")
    private Date uploadedon;

    @Column(name = "DOC_FLAG")
    private String docflag;

    @Column(name = "DOC_TYPE")
    private String doctype;

    @Column(name = "DOC_LINK")
    private String docLink;

    @Lob
    @Column(name = "DOCUMENT")
    private byte[] document;
}
