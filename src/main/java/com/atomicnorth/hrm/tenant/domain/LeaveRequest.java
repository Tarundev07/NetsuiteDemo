package com.atomicnorth.hrm.tenant.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "ses_m08_leave_request")
@Data
public class LeaveRequest extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEAVE_RF_NUM")
    private Long leaveRfNum;

    @Column(name = "REQUEST_NUMBER")
    private String requestNumber;

    @Column(name = "TOTAL_DAYS")
    private Double totalDays;

    @Column(name = "EMP_ID")
    private Integer empId;

    @Column(name = "LEAVE_CODE")
    private String leaveCode;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "START_OPTION")
    private String startOption;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "END_OPTION")
    private String endOption;

    @Column(name = "PURPOSE")
    private String purpose;

    @Column(name = "APPROVE_REMARK")
    private String approveRemark;

    @Column(name = "USER_REVERSAL_REMARK")
    private String userReversalRemark;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "LEAVE_YEAR")
    private String leaveYear;

    @Column(name = "REQUESTED_BY")
    private Integer requestedBy;
}