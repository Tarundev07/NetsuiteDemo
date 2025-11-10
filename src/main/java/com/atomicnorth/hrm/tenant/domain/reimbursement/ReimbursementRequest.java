package com.atomicnorth.hrm.tenant.domain.reimbursement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "ses_m00_rms_request")
public class ReimbursementRequest extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SR_NO")
    private Integer srno;

    @Column(name = "REQUEST_NUMBER")
    private String requestNumber;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "REQUESTED_AMOUNT")
    private Double requestedamount;

    @Column(name = "APPROVED_AMOUNT")
    private Double approvedamount;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "REVIEWER_REMARK")
    private String reviewerremark;

    @Column(name = "VP_REMARK")
    private String vpremark;

    @Column(name = "APPROVER_REMARK")
    private String approverremark;

    @Column(name = "BUCKET_ID")
    private Double bucketid;

    @Column(name = "BANK_REF_NUMBER")
    private String bankrefnumber;

    @Column(name = "PROCESSED_DATE")
    private Date processeddate;

    @Column(name = "REASSIGN_FLAG")
    private String reassignflag;
}
