package com.atomicnorth.hrm.tenant.domain.leave;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "ses_m08_leave_emp_balance")
@Data
public class LeaveEmpBalance extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEAVE_EMP_ID")
    private Integer leaveEmpId;

    @Column(name = "EMPLOYEE_ID", unique = true)
    private Integer employeeId;

    @Column(name = "LEAVE_CODE", unique = true)
    private Integer leaveCode;

    @Column(name = "LEAVE_BALANCE")
    private Double leaveBalance;

    @Column(name = "TOTAL_ALLOTTED_LEAVE")
    private Integer totalAllottedLeave;

    @Column(name = "LEAVE_YEAR")
    private String leaveYear;

    @Column(name = "BALANCE_UPDATION_FLAG", length = 1)
    private String balanceUpdationFlag;

    @Column(name = "REMARK")
    private String remark;

    @Column(name = "ENABLE_FLAG", length = 1)
    private String enableFlag;
}
