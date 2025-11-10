package com.atomicnorth.hrm.tenant.service.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskStoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer taskRfNum;

    private Integer projectRfNum;

    private String taskid;

    private String taskname;

    private String tasktype;

    private String taskdesc;

    private String projectid;
    private Integer projectMilestoneId;
    private String milestoneName;
    private String effortestimation;

    private String effortWeekCount;

    private String effortDayCount;

    private String effortHourCount;


    private Date plannedstartdate;

    private Date actualstartdate;

    private Date plannedenddate;

    private Date actualenddate;

    private String taskstatus;
    private String criticality;

    private String deleteflag;

    private String billableflag;

    private String workprogress;

    private Integer priceElementId;

    private String taskNatureCode;

    private String createdBy;
    private Instant createdDate;
    private String lastUpdatedBy;
    private Instant lastUpdatedDate;
    private List<SubTaskStoryDTO> subTaskStoryDTOList;

    private String day1;
    private String day1Comment;
    private String effortInHoursDay1;
    @DateTimeFormat
    private String day2;
    private String day2Comment;
    private String effortInHoursDay2;
    @DateTimeFormat
    private String day3;
    private String day3Comment;
    private String effortInHoursDay3;
    @DateTimeFormat
    private String day4;
    private String day4Comment;
    private String effortInHoursDay4;
    @DateTimeFormat
    private String day5;
    private String day5Comment;
    private String effortInHoursDay5;
    @DateTimeFormat
    private String day6;
    private String day6Comment;
    private String effortInHoursDay6;
    @DateTimeFormat
    private String day7;
    private String day7Comment;
    private String effortInHoursDay7;

    public TaskStoryDTO(Integer taskRfNum, String taskid, String taskname, String billableflag) {
        this.taskRfNum = taskRfNum;
        this.taskid = taskid;
        this.taskname= taskname;
        this.billableflag = billableflag;
    }
    public TaskStoryDTO(){

    }
}
