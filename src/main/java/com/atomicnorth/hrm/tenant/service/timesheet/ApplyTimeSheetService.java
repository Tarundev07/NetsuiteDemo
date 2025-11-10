package com.atomicnorth.hrm.tenant.service.timesheet;

import com.atomicnorth.hrm.tenant.domain.project.Project;
import com.atomicnorth.hrm.tenant.domain.project.ProjectAllocation;
import com.atomicnorth.hrm.tenant.domain.project.ProjectTaskAllocation;
import com.atomicnorth.hrm.tenant.domain.project.ProjectTemplateTask;
import com.atomicnorth.hrm.tenant.domain.project.TaskStory;
import com.atomicnorth.hrm.tenant.domain.timeSheet.UserTaskMapping;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.project.ProjectAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectTaskAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectTemplateRepository;
import com.atomicnorth.hrm.tenant.repository.project.TaskStoryRepository;
import com.atomicnorth.hrm.tenant.repository.timeSheet.UserTaskMappingRepository;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectListDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.TaskStoryDTO;
import com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO.TimesheetDTO;
import com.atomicnorth.hrm.tenant.service.leave.ApplyLeaveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplyTimeSheetService {
    private final Logger log = LoggerFactory.getLogger(ApplyTimeSheetService.class);
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserTaskMappingRepository userTaskMappingRepository;
    @Autowired
    private ProjectAllocationRepository projectAllocationRepository;
    @Autowired
    private TaskStoryRepository taskStoryRepository;
    @Autowired
    private ProjectTemplateRepository projectTemplateRepository;
    @Autowired
    private ProjectTaskAllocationRepository projectTaskAllocationRepository;
    @Autowired
    private ApplyLeaveService applyLeaveService;

    public List<ProjectListDTO> fetchLoggedInUserProjectList(Integer employeeId) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        if (employeeId == null) employeeId = user.getEmpId();
        List<ProjectAllocation> projectAllocation = projectAllocationRepository.findDistinctByEmployeeId(employeeId);
        List<Integer> projectRfNums = projectAllocation.stream().map(ProjectAllocation::getProjectRfNum).collect(Collectors.toList());
        List<Project> projects = projectRepository.findByProjectRfNumIn(projectRfNums);
        return projects.stream()
                .map(project -> {
                    ProjectListDTO dto = new ProjectListDTO();
                    dto.setProjectName(project.getProjectName());
                    dto.setProjectId(project.getProjectId());
                    dto.setProjectRfNum(project.getProjectRfNum());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TaskStoryDTO> getTaskListByProjectRfNum(Integer projectRfNum) throws SQLException {
        List<Integer> taskIds = new ArrayList<>();
        Project project = projectRepository.findById(projectRfNum).orElse(null);
        if (project != null) {
            if (project.getProjectTemplateId() != null) {
                projectTemplateRepository.findById(project.getProjectTemplateId()).ifPresent(projectTemplate -> taskIds.addAll(projectTemplate.getProjectTemplateTasks().stream().filter(x -> "Y".equals(x.getIsActive())).map(ProjectTemplateTask::getTaskId).collect(Collectors.toList())));
            }
        }
        List<ProjectTaskAllocation> allocations = projectTaskAllocationRepository.findByProjectRfNum(projectRfNum);
        taskIds.addAll(allocations.stream().map(ProjectTaskAllocation::getTaskRfNum).collect(Collectors.toList()));

        List<TaskStory> taskStories = taskStoryRepository.findByTaskRfNumIn(taskIds);
        return taskStories.stream().map(str -> new TaskStoryDTO(str.getTaskRfNum(), str.getTaskid(), str.getTaskname(), str.getBillableflag())).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> fetchUserTimeSheetData(String firstDate, String secondDate, String thirdDate,
                                                            String fourthDate, String fifthDate, String sixthDate,
                                                            String lastDate, String timesheetUsername) {
        Map<String, Object> finalResponse = new HashMap<>();
        List<Map<String, Object>> timesheetDetails = new ArrayList<>();
        List<Map<String, Object>> leaveDetails = new ArrayList<>();
        try {
            SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd");
            Date first = formats.parse(firstDate);
            Date last = formats.parse(lastDate);
            Integer employeeId = SessionHolder.getUserLoginDetail().getEmpId();
            List<UserTaskMapping> taskMappings  = userTaskMappingRepository.findDistinctTaskIdByUsernameAndTimesheetDateBetween(timesheetUsername, first, last);
            List<String> taskIdList = taskMappings.stream().map(UserTaskMapping::getTaskId).distinct().collect(Collectors.toList());
            for (String taskId : taskIdList) {
                List<UserTaskMapping> dataList = userTaskMappingRepository.findByUsernameAndTaskIdAndTimesheetDateBetweenAndTimesheetStatusNotOrderByTimesheetDateAsc(timesheetUsername, taskId, first, last, "deleted");
                for (UserTaskMapping obj : dataList) {
                    Map<String, Object> record = new HashMap<>();
                    String dateString = obj.getTimesheetDate() != null ? obj.getTimesheetDate().toString() : "";
                    record.put("timesheetId", obj.getTimesheetId());
                    record.put("projectId", obj.getProjectId());
                    record.put("taskId", obj.getTaskId());
                    record.put("taskName", obj.getTask().getTaskname());
                    record.put("timesheetDate", dateString);
                    record.put("effortInHours", obj.getEffortInHours());
                    record.put("submitStatus", obj.getSubmitStatus() != null && obj.getSubmitStatus().equalsIgnoreCase("true"));
                    record.put("remark", obj.getRemark());
                    record.put("approverRemark", obj.getApproverRemark());
                    record.put("timesheetStatus", obj.getTimesheetStatus());
                    record.put("projectName", obj.getProject().getProjectName());

                    String[] dateArray = {firstDate, secondDate, thirdDate, fourthDate, fifthDate, sixthDate, lastDate};
                    Map<String, String> dateMap = new HashMap<>();
                    for (int i = 0; i < dateArray.length; i++) {
                        dateMap.put(dateArray[i], String.valueOf(i + 1));
                    }

                    if (dateMap.containsKey(dateString)) {
                        String dayKey = "day" + dateMap.get(dateString);
                        record.put(dayKey, dateString);
                        record.put("effortInHours" + dayKey, obj.getEffortInHours());
                        record.put(dayKey + "Comment", obj.getRemark());
                        record.put("tmSheetStatus" + dayKey, obj.getTimesheetStatus());
                    }
                    timesheetDetails.add(record);
                }
            }

            List<String> allDates = Arrays.asList(firstDate, secondDate, thirdDate, fourthDate, fifthDate, sixthDate, lastDate);
            for (String dateStr : allDates) {
                LocalDate currentDate = LocalDate.parse(dateStr);

                Map<LocalDate, Map<String, String>> leaveMap = applyLeaveService.leaveAppliedOnDate(employeeId, currentDate);
                for (Map.Entry<LocalDate, Map<String, String>> entry : leaveMap.entrySet()) {
                    Map<String, Object> leaveRecord = new HashMap<>();
                    leaveRecord.put("date", entry.getKey().toString());
                    leaveRecord.put("type", entry.getValue().get("type"));
                    leaveRecord.put("status", entry.getValue().get("status"));
                    leaveDetails.add(leaveRecord);
                }
            }
            finalResponse.put("timesheetDetails", timesheetDetails);
            finalResponse.put("leaveDetails", leaveDetails);
        } catch (Exception e) {
            log.error("Error fetching user timesheet data for user: {}", timesheetUsername, e);
        }
        return finalResponse;
    }

    @Transactional
    public List<TimesheetDTO> applyTimesheet(List<TimesheetDTO> timesheetDTOs) throws ParseException {
        UserLoginDetail token = SessionHolder.getUserLoginDetail();
        SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd");
        List<TimesheetDTO> resultDTOs = new ArrayList<>();

        for (TimesheetDTO day : timesheetDTOs) {
            Date parsedDate = formats.parse(day.getTimesheetDate());
            java.sql.Date timesheetDate = new java.sql.Date(parsedDate.getTime());
            UserTaskMapping taskMap = userTaskMappingRepository.findByTimesheetId(day.getTimesheetId()).orElse(new UserTaskMapping());

            taskMap.setTimesheetId(day.getTimesheetId());
            taskMap.setTaskId(day.getTaskId());
            taskMap.setEmployeeId(day.getEmployeeId());
            taskMap.setUsername(day.getUserName());
            taskMap.setTimesheetDate(timesheetDate);
            taskMap.setEffortInHours(day.getEffortInHours());
            taskMap.setRemark(day.getRemark());
            taskMap.setTimesheetStatus(day.getTimesheetStatus());
            taskMap.setBillableFlag(day.getBillableFlag());
            taskMap.setProjectId(day.getProjectId());
            taskMap.setSubmitStatus(day.getSubmitStatus());
            taskMap.setLocked(true);
            taskMap.setCreatedBy(token.getUsername().toString());
            taskMap.setCreationDate(LocalDateTime.now());
            taskMap.setLastUpdatedBy(token.getUsername().toString());
            taskMap.setLastUpdateDate(LocalDateTime.now());

            UserTaskMapping savedTask = userTaskMappingRepository.save(taskMap);
            resultDTOs.add(new TimesheetDTO(savedTask));
        }

        return resultDTOs;
    }
}