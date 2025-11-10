package com.atomicnorth.hrm.tenant.service.dto.branch;

import com.atomicnorth.hrm.tenant.domain.branch.HRSettings;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class HRSettingDto {
    private Integer id;
    private Integer employeeId;
    private Double standardWorkingHours;
    private Double retirementAge;
    private Date workAnniversaries;
    private Date birthdays;
    private String sender;
    private String holidays;
    private Integer holidayReminderFrequency;
    private String sendLeaveNotification;
    private String leaveApprovalNotificationTemplate;
    private String expenseApproverMandatory;
    private String leaveStatusNotificationTemplate;
    private String showDeptLeavesOnCalendar;
    private String leaveApprovalMandatory;
    private String autoLeaveEncashment;
    private String restrictBackDatedLeave;
    private String allowMultipleShiftAssignment;
    private String checkVacancies;
    private String sendInterviewReminder;
    private String sendFeedbackInterview;
    private String exitQuestionnaireForm;
    private String exitQuestionnaireNotificationTemplate;
    private String allowEmployeeCheckInMobileApp;
    private String allowGeoLocationTracking;
    private String employeeAdvance;
    private String employeeName;

    public HRSettingDto(HRSettings hrSettings) {
        this.id = hrSettings.getId();
        this.employeeId = hrSettings.getEmployeeId();
        this.standardWorkingHours = hrSettings.getStandardWorkingHours();
        this.retirementAge = hrSettings.getRetirementAge();
        this.workAnniversaries = hrSettings.getWorkAnniversaries();
        this.birthdays = hrSettings.getBirthdays();
        this.holidays = hrSettings.getHolidays();
        this.holidayReminderFrequency = hrSettings.getHolidayReminderFrequency();
        this.sendLeaveNotification = hrSettings.getSendLeaveNotification();
        this.leaveApprovalNotificationTemplate = hrSettings.getLeaveApprovalNotificationTemplate();
        this.expenseApproverMandatory = hrSettings.getExpenseApproverMandatory();
        this.leaveStatusNotificationTemplate = hrSettings.getLeaveStatusNotificationTemplate();
        this.showDeptLeavesOnCalendar = hrSettings.getShowDeptLeavesOnCalendar();
        this.leaveApprovalMandatory = hrSettings.getLeaveApprovalMandatory();
        this.autoLeaveEncashment = hrSettings.getAutoLeaveEncashment();
        this.restrictBackDatedLeave = hrSettings.getRestrictBackDatedLeave();
        this.allowMultipleShiftAssignment = hrSettings.getAllowMultipleShiftAssignment();
        this.checkVacancies = hrSettings.getCheckVacancies();
        this.sendInterviewReminder = hrSettings.getSendInterviewReminder();
        this.sendFeedbackInterview = hrSettings.getSendFeedbackInterview();
        this.exitQuestionnaireForm = hrSettings.getExitQuestionnaireForm();
        this.exitQuestionnaireNotificationTemplate = hrSettings.getExitQuestionnaireNotificationTemplate();
        this.allowEmployeeCheckInMobileApp = hrSettings.getAllowEmployeeCheckInMobileApp();
        this.allowGeoLocationTracking = hrSettings.getAllowGeoLocationTracking();
        this.employeeAdvance = hrSettings.getEmployeeAdvance();
        this.employeeName = hrSettings.getEmployee().getFirstName() + " " + hrSettings.getEmployee().getLastName();
    }
}
