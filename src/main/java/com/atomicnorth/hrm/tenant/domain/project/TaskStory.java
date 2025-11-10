package com.atomicnorth.hrm.tenant.domain.project;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "ses_m02_task_story")
public class TaskStory extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TASK_RF_NUM")
    private Integer taskRfNum;

    @Column(name = "PROJECT_RF_NUM")
    private Integer projectRfNum;

    @Column(name = "TASK_ID")
    private String taskid;

    @Column(name = "TASK_NAME")
    private String taskname;

    @Column(name = "TASK_TYPE")
    private String tasktype;

    @Column(name = "TASK_DESC")
    private String taskdesc;

    @Column(name = "PROJECT_ID")
    private String projectid;

    @Column(name = "PROJECT_MILESTONE_ID")
    private Integer projectMilestoneId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_MILESTONE_ID", referencedColumnName = "PROJECT_MILESTONE_ID", insertable = false, updatable = false)
    private ProjectMilestone projectMilestone;

    @Column(name = "EFFORT_ESTIMATION")
    private String effortestimation;

    @Column(name = "EFFORT_WEEK_COUNT")
    private String effortWeekCount;

    @Column(name = "EFFORT_DAY_COUNT")
    private String effortDayCount;

    @Column(name = "EFFORT_HOUR_COUNT")
    private String effortHourCount;

    @Column(name = "PLANNED_START_DATE")
    @Temporal(TemporalType.DATE)
    private Date plannedstartdate;

    @Column(name = "ACTUAL_START_DATE")
    @Temporal(TemporalType.DATE)
    private Date actualstartdate;

    @Column(name = "PLANNED_END_DATE")
    @Temporal(TemporalType.DATE)
    private Date plannedenddate;

    @Column(name = "ACTUAL_END_DATE")
    @Temporal(TemporalType.DATE)
    private Date actualenddate;

    @Column(name = "TASK_STATUS")
    private String taskstatus;

    @Column(name = "CRITICALITY")
    private String criticality;

    @Column(name = "DELETE_FLAG")
    private String deleteflag;

    @Column(name = "BILLABLE_FLAG")
    private String billableflag;

    @Column(name = "WORK_PROGRESS")
    private String workprogress;

    @Column(name = "PRICE_ELEMENT_ID")
    private Integer priceElementId;

    @Column(name = "TASK_NATURE_CODE")
    private String taskNatureCode;

    @Column(name = "RECORD_INFO", columnDefinition = "TEXT")
    private String recordInfo;
    @JsonManagedReference
    @OneToMany(mappedBy = "taskStory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SubTaskStory> subTaskStories = new ArrayList<>();


}
