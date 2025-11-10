package com.atomicnorth.hrm.tenant.domain.employeeExit;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "emp_exit_request")
public class EmpExitRequest extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXIT_REQUEST_ID")
    private Integer id;

    @Column(name = "EMPLOYEE_ID", nullable = false)
    private Integer employeeId;

    @Column(name = "EXIT_REQUEST_NUMBER")
    private String exitRequestNumber;

    @Column(name = "EFFECTIVE_FROM")
    private LocalDate effectiveFrom;

    @Column(name = "EXIT_TYPE")
    private String exitType;

    @Column(name = "EXIT_REASON")
    private String exitReason;

    @Column(name = "LAST_WORKING_DATE")
    private LocalDate lastWorkingDate;

    @Column(name = "REQUEST_MEETING")
    private Boolean requestMeeting;

    @Column(name = "ELIGIBLE_TO_REHIRE")
    private Boolean eligibleToRehire;

    @Column(name = "REQUEST_BUYOUT")
    private Boolean requestBuyout;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "ATTACHMENT")
    private String attachment;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "APPROVAL_REQUIRED", insertable=false)
    private Boolean approvalRequired;

    @Column(name = "ASSET_CLEARANCE_STATUS", insertable=false)
    private String assetClearanceStatus;

    @Column(name = "FINANCE_CLEARANCE_STATUS", insertable=false)
    private String financeClearanceStatus;

    @Column(name = "KT_HANDOVER_STATUS", insertable=false)
    private String ktHandoverStatus;

    @Column(name = "ADMIN_CLEARANCE_STATUS", insertable=false)
    private String adminClearanceStatus;

    @Column(name = "EXIT_INTERVIEW_STATUS", insertable=false)
    private String exitInterviewStatus;

    @Column(name = "FULL_FINAL_STATUS", insertable=false)
    private String fullFinalStatus;

    @Column(name = "APPROVED_BY")
    private String approvedBy;

    @Column(name = "MANAGER_REMARK")
    private String managerRemark;

    @Column(name = "HR_REMARK")
    private String hrRemark;
    // ===== Relationships =====
    @OneToMany(mappedBy = "exitRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<EmpExitApproval> approvals;

    @OneToMany(mappedBy = "exitRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<EmpExitFinanceClearance> financeClearances;

    @OneToMany(mappedBy = "exitRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<EmpExitKtHandover> ktHandovers;

    @OneToMany(mappedBy = "exitRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<EmpExitAdminClearance> adminClearances;

    @OneToOne(mappedBy = "exitRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private EmpExitInterview interview;

    @OneToOne(mappedBy = "exitRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private EmpExitFullFinalSettlement settlement;
}
