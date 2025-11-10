package com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO;

import lombok.Data;

import java.util.Date;

@Data
public class UserTaskMappingDTO {
    private Integer timesheetId;
    private String effortInHours;
    private String taskId;
    private int week;
    private String weekDates;
    private String status;
    private String totalEffort;
    private String lastModifiedOn;
    private String joiningDate;
    private Date startDate;
    private Date endDate;
    private String projectId;
    private String date;
    private String dayName;
    private String task;
    private String effort;
    private String remark;
    private String accountManagerRemark;
    private boolean locked;
    private String projectCode;
    private String taskCode;
    private String projectName;
    private String taskName;
    private Integer username;
    private String timesheetDate;
    private String timesheetDayName;
    private double effortInHH;
    private double billableEffortInHH;
    private String userRemark;
    private String approverRemark;
    private String accManagerRemark;
    private String latestRemark;
    private boolean isBillable;
    private boolean isLocked;
    private String timesheetStatus;
    private String createdBy;
    private String createdByFullname;
    private String createdOn;
    private String lastUpdatedBy;
    private String lastUpdatedByFullname;
    private String lastUpdatedOn;
}
