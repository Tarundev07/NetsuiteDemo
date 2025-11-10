package com.atomicnorth.hrm.tenant.domain.approvalflow;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@Table(name = "workflow_status")
public class WorkflowStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WORKFLOW_STATUS_ID")
    private Integer workflowStatusId;

    @Column(name = "LEVEL")
    private Integer level;

    @Column(name = "ORDER_BY")
    private Integer displayOrder;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "APPROVAL_DATE")
    private LocalDate approvalDate;

    @Column(name = "APPROVAL_BY")
    private Integer approvalBy;

    @Column(name = "ASSIGN_TO")
    private Integer assignTo;

    @Column(name = "ASSIGN_DATE")
    private Date assignDate;

    @Column(name = "DELEGATION_TO")
    private String delegationTo;

    @Column(name = "DELEGATION_DATE")
    private LocalDate delegationDate;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "MAIL_SEND")
    private String mailSend;

    @Column(name = "SMS_SEND")
    private String smsSend;

    @Column(name = "WORKFLOW_REQUEST_ID")
    private Integer workflowRequestId;
}
