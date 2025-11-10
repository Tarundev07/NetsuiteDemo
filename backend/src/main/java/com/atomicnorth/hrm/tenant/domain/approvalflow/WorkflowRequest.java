package com.atomicnorth.hrm.tenant.domain.approvalflow;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

@Entity
@Data
@Table(name = "workflow_request")
public class WorkflowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WORKFLOW_REQUEST_ID")
    private Integer workflowRequestId;

    @Column(name = "WORKFLOW_MASTER_ID")
    private Integer workflowMasterId;

    @Column(name = "REQUEST_NUMBER")
    private String requestNumber;

    @Column(name = "FUNCTION_ID")
    private Integer functionId;

    @Column(name = "REQUEST_DATE")
    private Date requestDate;

    @Column(name = "REQUEST_STATUS")
    private String requestStatus;

    @Column(name = "REQUEST_CLOSED_ON")
    private Date requestClosedOn;

    @Transient
    private String userStatus;
}
