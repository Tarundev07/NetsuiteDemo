package com.atomicnorth.hrm.tenant.domain.attendance;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ses_m07_shift_emp")
public class ShiftEmployeeEntity  extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHIFT_EMP_ID")
    private Integer shiftEmpId;

    @Column(name = "SHIFT_ID")
    private Integer shiftId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "END_DATE")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "CLIENT_ID")
    private Integer clientId;
    @Column(name = "LAST_UPDATE_SESSION_ID")
    private Integer lastUpdateSessionId;


}
