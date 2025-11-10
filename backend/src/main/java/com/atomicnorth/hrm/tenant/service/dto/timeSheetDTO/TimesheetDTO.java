package com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO;

import com.atomicnorth.hrm.tenant.domain.timeSheet.UserTaskMapping;
import lombok.Data;

@Data
public class TimesheetDTO {
    private Integer timesheetId;
    private String taskId;
    private String userName;
    private Integer employeeId;
    private String timesheetDate;
    private String effortInHours;
    private String remark;
    private String timesheetStatus;
    private String billableFlag;
    private String projectId;
    private String submitStatus;

    private String projectName;
    private String taskName;
    private String accountManagerRemark;

    public TimesheetDTO(UserTaskMapping task) {
        this.timesheetId = task.getTimesheetId();
        this.taskId = task.getTaskId();
        this.userName = task.getUsername();
        this.employeeId = task.getEmployeeId();
        this.timesheetDate = task.getTimesheetDate().toString();
        this.effortInHours = task.getEffortInHours();
        this.remark = task.getRemark();
        this.timesheetStatus = task.getTimesheetStatus();
        this.billableFlag = task.getBillableFlag();
        this.projectId = task.getProjectId();
        this.submitStatus = task.getSubmitStatus();
    }

    public TimesheetDTO() {

    }
}
