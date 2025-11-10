package com.atomicnorth.hrm.tenant.domain.branch;

import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import com.atomicnorth.hrm.tenant.domain.Employee;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Data
@Entity
@Table(name = "ses_m00_hrms_settings")
public class HRSettings extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;
    @Column(name = "STANDARD_WORKING_HOURS")
    private Double standardWorkingHours;
    @Column(name = "RETIREMENT_AGE")
    private Double retirementAge;
    @Column(name = "WORK_ANNIVERSARIES")
    @Temporal(TemporalType.DATE)
    private Date workAnniversaries;
    @Column(name = "BIRTHDAYS")
    @Temporal(TemporalType.DATE)
    private Date birthdays;
    @Column(name = "SENDER")
    private String sender;
    @Column(name = "HOLIDAYS")
    private String holidays;
    @Column(name = "HOLIDAY_REMINDER_FREQUENCY")
    private Integer holidayReminderFrequency;
    @Column(name = "SEND_LEAVE_NOTIFICATION")
    private String sendLeaveNotification;
    @Column(name = "LEAVE_APPROVAL_NOTIFICATION_TEMPLATE")
    private String leaveApprovalNotificationTemplate;
    @Column(name = "EXPENSE_APPROVER_MANDATORY")
    private String expenseApproverMandatory;
    @Column(name = "LEAVE_STATUS_NOTIFICATION_TEMPLATE")
    private String leaveStatusNotificationTemplate;
    @Column(name = "SHOW_DEPT_LEAVES_IN_CALENDAR")
    private String showDeptLeavesOnCalendar;
    @Column(name = "LEAVE_APPROVER_MANDATORY")
    private String leaveApprovalMandatory;
    @Column(name = "AUTO_LEAVE_ENCASHMENT")
    private String autoLeaveEncashment;
    @Column(name = "RESTRICT_BACKDATED_LEAVE")
    private String restrictBackDatedLeave;
    @Column(name = "ALLOW_MULTIPLE_SHIFT_ASSIGNMENTS")
    private String allowMultipleShiftAssignment;
    @Column(name = "CHECK_VACANCIES")
    private String checkVacancies;
    @Column(name = "SEND_INTERVIEW_REMINDER")
    private String sendInterviewReminder;
    @Column(name = "SEND_FEEDBACK_REMINDER")
    private String sendFeedbackInterview;
    @Column(name = "EXIT_QUESTIONNAIRE_FORM")
    private String exitQuestionnaireForm;
    @Column(name = "EXIT_QUESTIONNAIRE_NOTIFICATION_TEMPLATE")
    private String exitQuestionnaireNotificationTemplate;
    @Column(name = "ALLOW_EMPLOYEE_CHECK_IN_MOBILE_APP")
    private String allowEmployeeCheckInMobileApp;
    @Column(name = "ALLOW_GEOLOCATION_TRACKING")
    private String allowGeoLocationTracking;
    @Column(name = "UNLINK_PAYMENT_ON_EMPLOYEE_ADVANCE_CANCELLATION")
    private String employeeAdvance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "EMPLOYEE_ID", insertable = false, updatable = false)
    private Employee employee;


    public void setBirthdays(Date birthdays) {
        if (birthdays != null) {
            LocalDate today = LocalDate.now();
            LocalDate providedDate = birthdays.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (providedDate.isAfter(today)) {
                throw new IllegalArgumentException("birthdays date cannot be in the future");
            }
        }
        this.birthdays = birthdays;
    }
}
