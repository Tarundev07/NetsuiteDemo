package com.atomicnorth.hrm.tenant.domain.reimbursement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "ses_m00_rms_request_mapping")
public class ReimbursementRequestMapping extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REQUEST_ID")
    private Integer requestId;

    @Column(name = "REQUEST_NUMBER")
    private String requstnumber;

    @Column(name = "EXPENSE_CODE")
    private String expensecode;

    @Column(name = "PROJECT_ID")
    private Integer projectId;

    @Column(name = "TASK_ID")
    private String taskId;

    @Column(name = "EXPENSE_DESC")
    private String expensedesc;

    @Column(name = "BILL_NUMBER")
    private String billnumber;

    @Column(name = "BILL_DATE")
    private Date billdate;
    @Column(name = "REQUEST_AMOUNT")
    private Double requestamount;

    @Column(name = "APPROVED_AMOUNT")
    private Double approvedamount;

    @Column(name = "BILL_AMOUNT")
    private Double billamount;

    @Column(name = "APPLICANT_REMARK")
    private String applicantremark;

    @Column(name = "ATTACHMENT")
    private String attachment;
    @Column(name = "MAPPING_FLAG")
    private String mappingFlag;
    @Column(name="STATUS")
    private String status;
}
