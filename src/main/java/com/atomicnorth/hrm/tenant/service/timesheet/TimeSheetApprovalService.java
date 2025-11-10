package com.atomicnorth.hrm.tenant.service.timesheet;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.project.ProjectAllocation;
import com.atomicnorth.hrm.tenant.domain.timeSheet.UserTaskMapping;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.timeSheet.ActionHistoryRepository;
import com.atomicnorth.hrm.tenant.repository.timeSheet.UserTaskMappingRepository;
import com.atomicnorth.hrm.tenant.service.attendance.AttendanceMoafService;
import com.atomicnorth.hrm.tenant.service.dto.leave.LeaveRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO.UserTimesheetDTO;
import com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO.WeeklyTimesheetSummary;
import com.atomicnorth.hrm.tenant.service.leave.ApplyLeaveService;
import com.atomicnorth.hrm.util.ActivityLog;
import com.atomicnorth.hrm.util.EmailUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TimeSheetApprovalService {
    private final EntityManager entityManager;
    private final UserTaskMappingRepository userTaskMappingRepository;
    private final Logger log = LoggerFactory.getLogger(TimeSheetApprovalService.class);
    @Autowired
    ActivityLog activityLog;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private EmailUtility emailUtility;
    @Autowired
    private ActionHistoryRepository actionHistoryRepository;
    @Autowired
    private AttendanceMoafService attendanceMoafService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Value("${HR}")
    private String HR;
    @Autowired
    private ProjectAllocationRepository projectAllocationRepository;
    @Autowired
    private ApplyLeaveService applyLeaveService;

    @Autowired
    public TimeSheetApprovalService(EntityManager entityManager, UserTaskMappingRepository userTaskMappingRepository) {
        this.entityManager = entityManager;
        this.userTaskMappingRepository = userTaskMappingRepository;
    }

    public boolean checkUserAuthorization(String username) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        try {
            if (username.equalsIgnoreCase(tokenHolder.getUsername().toString()))
                return true;
            if ((tokenHolder.getAuthorities()).contains(String.valueOf(Constant.ACCESS_GROUP_PG_IN_ADMIN)) || (tokenHolder.getAuthorities()).contains(HR))
                return true;
            else
                return userTaskMappingRepository.countByUsernameAndReportingPersonOrHrManager(username, String.valueOf(tokenHolder.getUsername()), String.valueOf(tokenHolder.getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public List<Map<String, Object>> getDataByPrjctUser(String startDate, String endDate, String username) {
        List<Map<String, Object>> resultData = new ArrayList<>();

        try {
            List<Object[]> attendanceDataList = userTaskMappingRepository.getUserTaskMappings(username, startDate, endDate);
            for (Object[] attendanceData : attendanceDataList) {
                Map<String, Object> eachData = new HashMap<>();
                double weekEffortOnTask = 0.0;
                // Populate basic data
                eachData.put("timeSheetId", attendanceData[0] != null ? attendanceData[0].toString() : "");
                eachData.put("projectId", attendanceData[1] != null ? attendanceData[1].toString() : "");
                eachData.put("taskId", attendanceData[2] != null ? attendanceData[2].toString() : "");
                eachData.put("timesheetDate", attendanceData[3] != null ? attendanceData[3].toString() : "");
                eachData.put("effortInHours", attendanceData[4] != null ? attendanceData[4].toString() : "");
                eachData.put("timesheetStatus", attendanceData[5] != null ? attendanceData[5].toString() : "");
                eachData.put("remark", attendanceData[6] != null ? attendanceData[6].toString() : "");
                eachData.put("username", attendanceData[7] != null ? attendanceData[7].toString() : "");
                eachData.put("approverRemark", attendanceData[8] != null ? attendanceData[8].toString() : "");
                eachData.put("accountManagerRemark", attendanceData[9] != null ? attendanceData[9].toString() : "");
                eachData.put("billAbleEffortInHours", attendanceData[10] != null ? attendanceData[10].toString() : "");
                eachData.put("billableFlag", attendanceData[11] != null ? attendanceData[11].toString() : "");
                eachData.put("projectName", attendanceData[12] != null ? attendanceData[12].toString() : "");
                eachData.put("taskName", attendanceData[13] != null ? attendanceData[13].toString() : "");
                eachData.put("userFullName", attendanceData[14] != null ? attendanceData[14].toString() : "");
                // Compute weekly effort
                weekEffortOnTask += attendanceData[4] != null
                        ? Double.parseDouble(attendanceData[4].toString())
                        : 0.0;
                eachData.put("usereffort", weekEffortOnTask);
                eachData.put("remarkUser", "NA".equalsIgnoreCase(String.valueOf(attendanceData[6]))
                        ? "No comments available"
                        : attendanceData[6]);
                resultData.add(eachData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultData;
    }

    @Transactional
    public boolean updateTimesheetStatusUser(String startDate, String endDate, boolean result, String username, String appRmrk) throws ParseException, SQLException {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();

        boolean status = false;
        String resultString = "";
        if (appRmrk.trim().isEmpty()) {
            if (result)
                appRmrk = "Ok";
            else
                appRmrk = "Invalid effort entry";
        }
        SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd");
        Date parsed1 = formats.parse(startDate);
        java.sql.Date startDate1 = new java.sql.Date(parsed1.getTime());

        Date parsed2 = formats.parse(endDate);
        java.sql.Date endDate1 = new java.sql.Date(parsed2.getTime());
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, dt);
        LocalDate end = LocalDate.parse(endDate, dt);
        Integer employeeId = userTaskMappingRepository.findByUsername(username).stream().findFirst().map(UserTaskMapping::getEmployeeId).orElse(null);
        boolean appliedLeaves = applyLeaveService.isLeaveAppliedForDateRange(employeeId, start, end, "PENDING");
        if (appliedLeaves) {
            throw new IllegalArgumentException("Leave approval is pending for one or more days.");
        }
        try {
            if (result) {
                resultString = "Approved";
                userTaskMappingRepository.updateUserTaskMappingStatus(resultString, sdf.format(new Date()), tokenHolder.getUsername().toString(), username, startDate1.toString(), endDate1.toString());
                insertIntoActionHistory("Approved", username, appRmrk, startDate1.toString(), endDate1.toString());
                if (emailUtility.checkMailNotificationTrigger(Constant.EMAIL_EVENT_TS_APPROVAL)) {
                    String body = "Your time sheet for week " + startDate1 + " to " + endDate1 + " has been approved.";
                }
                activityLog.captureUserActivity(String.valueOf(Constant.MODULE_ID_TIMESHEET), username, "TS_" + startDate + "_" + endDate, String.valueOf(Constant.MODULE_ID_TIMESHEET), "Timesheet approved for week " + startDate + " to " + endDate, tokenHolder.getUsername().toString());
            } else {
                resultString = "Rejected";
                // GetQueryAPI.getQuery(TM39, resultString,sdf.format(new Date()),tokenHolder.getUsername().toString(),username,startDate1.toString(),endDate1.toString());
                userTaskMappingRepository.updateUserTaskMappingStatusWithFalse(resultString, sdf.format(new Date()), tokenHolder.getUsername().toString(), username, startDate1.toString(), endDate1.toString());
                insertIntoActionHistory("Rejected", username, appRmrk, startDate1.toString(), endDate1.toString());
                if (emailUtility.checkMailNotificationTrigger(Constant.EMAIL_EVENT_TS_REJECTION)) {
                    String body = "Your time sheet for week " + startDate1 + " to " + endDate1 + " has been rejected.";
                    // doSendEmail(getUserMailId(username), "Timesheet Rejected", body);
                }
                activityLog.captureUserActivity(String.valueOf(Constant.MODULE_ID_TIMESHEET), username, "TS_" + startDate + "_" + endDate, String.valueOf(Constant.MODULE_ID_TIMESHEET), "Timesheet rejected for week " + startDate + " to " + endDate, tokenHolder.getUsername().toString());
            }
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public Object insertIntoActionHistory(String actionType,
                                          String username, String remark, String startDate, String endDate) throws SQLException, ParseException {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        boolean status = false;
        SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat hhFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date parsed1 = formats.parse(startDate);
        java.sql.Date startDate1 = new java.sql.Date(parsed1.getTime());

        Date parsed2 = formats.parse(endDate);
        java.sql.Date endDate1 = new java.sql.Date(parsed2.getTime());
        try {
            return actionHistoryRepository.insertActionHistory(actionType, username, tokenHolder.getUsername().toString(), remark, hhFormat.format(new Date()), endDate1.toString(), startDate1.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public void removeSourceTSAttendanceLog(String startDate, String endDate, String username) {
        try {
            Map<String, List<String>> userShiftTimeMap = attendanceMoafService.getUserShiftTimeMap(startDate, endDate, username);
            String logTimeStart = startDate + " " + userShiftTimeMap.get(startDate).get(1);
            String logTimeEnd = endDate + " " + userShiftTimeMap.get(endDate).get(1);
            if ("Y".equalsIgnoreCase(userShiftTimeMap.get(endDate).get(3)))
                logTimeEnd = LocalDate.parse(endDate).plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " + userShiftTimeMap.get(endDate).get(2);
            actionHistoryRepository.deleteByEmpIdAndDeviceIdAndLogTimeRange(userShiftTimeMap.get(startDate).get(4), Constant.USER_ATTENDANCE_SOURCE_TIMESHEET, logTimeStart, logTimeEnd);
            actionHistoryRepository.deleteLogsByEmpIdAndDeviceIdAndLogTimeRange(userShiftTimeMap.get(startDate).get(4), Constant.USER_ATTENDANCE_SOURCE_TIMESHEET, logTimeStart, logTimeEnd);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occured||Method:removeSourceTSAttendanceLog||Cause:" + e.getMessage());
        }
    }

    public String saveUserTimesheet(UserTimesheetDTO timesheetBean) {
        UserLoginDetail currentUser = SessionHolder.getUserLoginDetail();
        List<UserTaskMapping> updatedEntities = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        List<WeeklyTimesheetSummary> skippedRecords = new ArrayList<>();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        LocalDateTime currentDateTime = LocalDateTime.now();
        Long username = currentUser.getUsername();
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (WeeklyTimesheetSummary row : timesheetBean.getBulkRequest()) {
            try {
                String[] dates = row.getWeekDates().split(" - ");
                Date startDate = sdf2.parse(dates[0]);
                Date endDate = sdf2.parse(dates[1]);
                LocalDate start = LocalDate.parse(dates[0], dt);
                LocalDate end = LocalDate.parse(dates[1], dt);
                Integer employeeId = userTaskMappingRepository.findByUsername(row.getUsername()).stream().findFirst().map(UserTaskMapping::getEmployeeId).orElse(null);
                boolean appliedLeaves = applyLeaveService.isLeaveAppliedForDateRange(employeeId, start, end, "PENDING");
                if (appliedLeaves) {
                    skippedRecords.add(row);
                    continue;
                }
                List<UserTaskMapping> tmsData = userTaskMappingRepository.findByTimesheetDateBetween(startDate, endDate);
                for (UserTaskMapping data : tmsData) {
                    if (timesheetBean.getStatus()) {
                        data.setTimesheetStatus("Approved");
                        data.setBillableEffortInHours(data.getEffortInHours() != null ? Integer.parseInt(data.getEffortInHours()) : 0);
                        data.setBillableFlag("Y");
                    } else {
                        data.setTimesheetStatus("Rejected");
                        data.setBillableEffortInHours(0);
                        data.setBillableFlag("N");
                    }
                    data.setLastModifiedDate(currentDate);
                    data.setLastUpdateDate(currentDateTime);
                    data.setLastUpdatedBy(String.valueOf(username));

                    updatedEntities.add(data);
                }
            } catch (Exception e) {
                failedIds.add(String.valueOf(row.getTimesheetId()));
                log.error("Failed to update timesheet ID {}: {}", row.getTimesheetId(), e.getMessage(), e);
                throw new IllegalArgumentException("Some rows failed to update. Failed IDs: " + String.join(", ", failedIds));
            }
        }

        if (!updatedEntities.isEmpty()) {
            userTaskMappingRepository.saveAll(updatedEntities);
        }
        if (!skippedRecords.isEmpty()) {
            return "Timesheet updated partially. Skipped " + skippedRecords.size() + " rows due to pending leaves.";
        }
        return "Timesheet data updated successfully.";
    }

    public Page<Map<String, Object>> getUsersDataUnderReportee(String username, String startDate, String endDate,
                                                               String filterVar, String allReporteeFlag,
                                                               String departments, String divisions,
                                                               String reportingManagers, Pageable pageable) {
        Page<Map<String, Object>> userPage = Page.empty();
        try {
            UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
            List<String> loggedInUserGroups = Arrays.asList(tokenHolder.getAuthorities().split(","));

            log.info("loggedInUserGroupsGetAuthorities--{}", loggedInUserGroups);
            Page<Object[]> resultPage = Page.empty();

            // If filterVar is not 'All', fetch data for the specified filters
            if (!filterVar.equalsIgnoreCase("All")) {
                resultPage = actionHistoryRepository.findWeeklySummaryByUsernameAndDate(username, startDate, endDate,
                        filterVar, departments, divisions, reportingManagers, pageable);
            }

            // Convert the Object[] results to Map<String, Object>
            List<Map<String, Object>> userList = new ArrayList<>();
            for (Object[] row : resultPage.getContent()) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("username", row[0] != null ? row[0].toString() : "");
                userMap.put("usereffort", row[1] != null ? row[1] : 0);
                userMap.put("userfName", row[2] != null ? row[2].toString() : "");
                userMap.put("usercode", row[3] != null ? row[3].toString() : "");
                userMap.put("rmManager", row[4] != null ? row[4].toString() : "");
                userMap.put("rmManagerName", row[5] != null ? row[5].toString() : "");
                userMap.put("approverRemark", row[6] != null ? row[6].toString() : "");
                userMap.put("status", row[7] != null ? row[7].toString() : "");
                userMap.put("weekDates", row[9] != null ? row[9].toString() : "");
                userMap.put("id", row[10] != null ? row[10].toString() : "");

                userList.add(userMap);
            }

            userPage = new PageImpl<>(userList, pageable, resultPage.getTotalElements());

        } catch (Exception e) {
            log.error("Error fetching user data under reportee", e);
        }

        return userPage;
    }

    @Transactional
    public Map<String, Object> getTSByProject(Integer projectId, String startDate, String endDate, String status, String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable) throws ParseException {
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Date start = sdf2.parse(startDate);
        Date end = sdf2.parse(endDate);

        List<ProjectAllocation> allocations = projectAllocationRepository.findByProjectRfNumAndStartDateLessThanEqualAndEndDateGreaterThanEqual(projectId, end, start);

        List<Integer> allocatedEmployeeIds = allocations.stream().map(ProjectAllocation::getEmployeeId).collect(Collectors.toList());

        List<UserTaskMapping> tmsData = userTaskMappingRepository.findByEmployeeIdInAndTimesheetDateBetweenAndTimesheetStatusNot(allocatedEmployeeIds, start, end, "Deleted");
        if (status != null) {
            if (!status.isBlank() && !status.equalsIgnoreCase("all")) {
                tmsData = tmsData.stream().filter(ts -> status.equalsIgnoreCase(ts.getTimesheetStatus())).collect(Collectors.toList());
            }
        }

        Map<Integer, Map<LocalDate, List<UserTaskMapping>>> grouped = tmsData.stream()
                .collect(Collectors.groupingBy(
                        UserTaskMapping::getEmployeeId,
                        Collectors.groupingBy(ts -> getWeekStart(ts.getTimesheetDate()))
                ));

        List<WeeklyTimesheetSummary> summaries = new ArrayList<>();
        for (Map.Entry<Integer, Map<LocalDate, List<UserTaskMapping>>> empEntry : grouped.entrySet()) {
            Integer empId = empEntry.getKey();
            for (Map.Entry<LocalDate, List<UserTaskMapping>> weekEntry : empEntry.getValue().entrySet()) {
                LocalDate weekStart = weekEntry.getKey();
                LocalDate weekEnd = weekStart.plusDays(6);
                List<UserTaskMapping> weekTs = weekEntry.getValue();

                BigDecimal totalEffort = weekTs.stream()
                        .map(ts -> {
                            try {
                                return new BigDecimal(ts.getEffortInHours());
                            } catch (Exception e) {
                                return BigDecimal.ZERO;
                            }
                        }).reduce(BigDecimal.ZERO, BigDecimal::add);

                Set<String> statuses = weekTs.stream().map(UserTaskMapping::getTimesheetStatus).filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toSet());

                String finalStatus;
                if (statuses.size() == 1) {
                    String singleStatus = statuses.iterator().next();
                    if ("approved".equals(singleStatus)) {
                        finalStatus = "Approved";
                    } else if ("rejected".equals(singleStatus)) {
                        finalStatus = "Rejected";
                    } else {
                        finalStatus = "Pending";
                    }
                } else {
                    finalStatus = "Pending";
                }

                UserTaskMapping first = weekTs.get(0);
                Employee emp = employeeRepository.findById(empId).orElse(null);

                WeeklyTimesheetSummary dto = new WeeklyTimesheetSummary();
                dto.setEmployeeId(empId);
                dto.setWeekStart(weekStart);
                dto.setWeekEnd(weekEnd);
                dto.setTotalEffort(totalEffort);
                dto.setEmployeeName(emp != null ? emp.getFullName() + " (" + emp.getEmployeeNumber() + ")" : "Unknown");
                String formattedWeekStart = weekStart.format(formatter);
                String formattedWeekEnd = weekEnd.format(formatter);
                dto.setWeekDates(formattedWeekStart + " - " + formattedWeekEnd);
                dto.setUsername(first.getUsername());
                dto.setTimesheetId(first.getTimesheetId());
                dto.setReportingManagerName(
                        emp != null && emp.getReportingManager() != null
                                ? emp.getReportingManager().getFirstName() + " " + emp.getReportingManager().getLastName()
                                : "Unknown");
                dto.setStatus(finalStatus);
                dto.setApproverRemark(first.getApproverRemark());

                summaries.add(dto);
            }
        }

        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {
            summaries = summaries.stream()
                    .filter(dto -> {
                        try {
                            Field field = WeeklyTimesheetSummary.class.getDeclaredField(searchField);
                            field.setAccessible(true);
                            Object fieldValue = field.get(dto);
                            return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Field sortField = WeeklyTimesheetSummary.class.getDeclaredField(sortBy);
                sortField.setAccessible(true);

                Comparator<WeeklyTimesheetSummary> comparator = Comparator.comparing(dto -> {
                    try {
                        Object val = sortField.get(dto);
                        return (Comparable<Object>) val;
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }, Comparator.nullsLast(Comparator.naturalOrder()));

                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }

                summaries.sort(comparator);

            } catch (NoSuchFieldException e) {
                log.info("Invalid sort field: {}", sortBy);
            }
        }

        int totalItems = summaries.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        List<WeeklyTimesheetSummary> paginatedList =
                (startIndex < totalItems) ? summaries.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedList);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);

        return response;
    }

    private LocalDate getWeekStart(Date date) {
        LocalDate localDate = ((java.sql.Date) date).toLocalDate();
        return localDate.getDayOfWeek() == DayOfWeek.SUNDAY ? localDate : localDate.minusDays(localDate.getDayOfWeek().getValue());
    }

    public void checkTimesheetExists(LeaveRequestDTO leaveRequestDTO) {
        String tokenUsername = SessionHolder.getUserLoginDetail().getUsername().toString();
        Map<String, LocalDate> weekRange = getWeekRange(leaveRequestDTO.getStartDate());
        LocalDate weekStartLocal = weekRange.get("weekStart");
        LocalDate weekEndLocal = weekRange.get("weekEnd");
        Date weekStart = Date.from(weekStartLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date weekEnd = Date.from(weekEndLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        java.sql.Date start = new java.sql.Date(weekStart.getTime());
        java.sql.Date end = new java.sql.Date(weekEnd.getTime());
        List<UserTaskMapping> timesheetData = userTaskMappingRepository.findByEmployeeIdAndTimesheetDateBetweenAndTimesheetStatus(leaveRequestDTO.getEmpId(), weekStart, weekEnd, "Pending");
        if (!timesheetData.isEmpty()) {
            String username = timesheetData.stream().map(UserTaskMapping::getUsername).findFirst().get();
            userTaskMappingRepository.updateUserTaskMappingStatusWithFalse("Rejected", sdf.format(new Date()), tokenUsername, username, start.toString(), end.toString());
        }
    }

    public Map<String, LocalDate> getWeekRange(LocalDate localDate) {
        LocalDate weekStart = localDate.getDayOfWeek() == DayOfWeek.SUNDAY ? localDate : localDate.minusDays(localDate.getDayOfWeek().getValue());
        LocalDate weekEnd = weekStart.plusDays(6);

        Map<String, LocalDate> weekRange = new HashMap<>();
        weekRange.put("weekStart", weekStart);
        weekRange.put("weekEnd", weekEnd);

        return weekRange;
    }

}