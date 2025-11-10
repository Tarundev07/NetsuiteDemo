package com.atomicnorth.hrm.tenant.domain.reimbursement;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ses_m00_rms_action_history")
public class ReimbursementActionHistory extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACTION_ID")
    private Integer actionid;

    @Column(name = "ACTION_NAME")
    private String actionname;

    @Column(name = "REQUEST_NUMBER")
    private String requestnumber;

    @Column(name = "USER_NAME")
    private String username;

    @Column(name = "ACTION_BY")
    private String actionby;

    @Column(name = "REMARK")
    private String remark;

    @Column(name = "ASSIGNED_TO")
    private String assignedto;
}
