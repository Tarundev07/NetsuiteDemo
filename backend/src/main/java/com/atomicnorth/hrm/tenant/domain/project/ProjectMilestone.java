package com.atomicnorth.hrm.tenant.domain.project;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "ses_m02_project_milestone")
public class ProjectMilestone extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROJECT_MILESTONE_ID")
    private Integer projectMilestoneId;

    @Column(name = "MILESTONE_NAME")
    private String milestoneName;

    @Column(name = "PROJECT_RF_NUM")
    private Integer projectRfNum;

    @Column(name = "PROJECT_MILESTONE_TYPE_ID")
    private Integer projectMilestoneTypeId;

    @Column(name = "SEQUENCE_ORDER")
    private Integer sequenceOrder;

    @Column(name = "AMOUNT")
    private Double amount;

    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "REMARK")
    private String remark;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "IS_DELETED")
    private String isDeleted;

}
