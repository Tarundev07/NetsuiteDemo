package com.atomicnorth.hrm.tenant.domain.project;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "ses_m02_sub_task_story")
public class SubTaskStory extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUB_TASK_RF_NUM", nullable = false)
    private Integer subTaskRfNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASK_RF_NUM", nullable = false)
    private TaskStory taskStory;

    @Column(name = "SUB_TASK_ID", length = 250)
    private String subTaskId;

    @Column(name = "IS_ACTIVE", length = 1)
    private String isActive;
    @Column(name = "DELETE_FLAG", length = 1)
    private String deleteFlag;
    @Column(name = "RECORD_INFO", columnDefinition = "TEXT")
    private String recordInfo;
}
