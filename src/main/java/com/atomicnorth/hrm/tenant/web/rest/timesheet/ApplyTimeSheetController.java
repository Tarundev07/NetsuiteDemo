package com.atomicnorth.hrm.tenant.web.rest.timesheet;

import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.service.dto.project.ProjectListDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.TaskStoryDTO;
import com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO.TimesheetDTO;
import com.atomicnorth.hrm.tenant.service.timesheet.ApplyTimeSheetService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/timesheet")
public class ApplyTimeSheetController {

    private final Logger log = LoggerFactory.getLogger(ApplyTimeSheetController.class);
    @Autowired
    private ApplyTimeSheetService applyTimeSheetService;

    @GetMapping("/getLoggedInUserProjectList")
    public ResponseEntity<ApiResponse<List<ProjectListDTO>>> getProjectList(@RequestParam(value = "employeeId", required = false) Integer employeeId) {

        try {
            List<ProjectListDTO> projectData = applyTimeSheetService.fetchLoggedInUserProjectList(employeeId);
            ApiResponse<List<ProjectListDTO>> response = new ApiResponse<>(
                    projectData,
                    true,
                    "ATMCMN_PROJECTS_FETCHED",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DataAccessException e) {
            log.error("Database error while fetching user project list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, false, "ATMCMN_DATABASE_ERROR", "FAILURE", Collections.singletonList("Database error occurred")));
        } catch (Exception e) {
            log.error("Unexpected error while fetching user project list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, false, "ATMCMN_FETCH_ERROR", "FAILURE", Collections.singletonList("An unexpected error occurred")));
        }
    }

    @GetMapping("/getTaskList")
    public ResponseEntity<ApiResponse<List<TaskStoryDTO>>> getTaskList(
            @RequestParam(value = "projectRfNum", required = true) Integer projectRfNum) {
        if (projectRfNum == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, false, "INVALID_PROJECT_ID", "FAILURE",
                            Collections.singletonList("Project ID must not be empty")));
        }
        try {
            List<TaskStoryDTO> taskList = applyTimeSheetService.getTaskListByProjectRfNum(projectRfNum);
            return ResponseEntity.ok(new ApiResponse<>(taskList, true, "TASKS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching task list for projectId: {}", projectRfNum, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "FETCH_ERROR", "FAILURE",
                            Collections.singletonList("An unexpected error occurred while fetching task list")));
        }
    }

    @GetMapping("/fetchCurrentWeekData")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchCurrentWeekData(@RequestParam(value = "firstDate") String firstDate, @RequestParam(value = "secondDate") String secondDate, @RequestParam(value = "thirdDate") String thirdDate, @RequestParam(value = "fourthDate") String fourthDate, @RequestParam(value = "fifthDate") String fifthDate, @RequestParam(value = "sixthDate") String sixthDate, @RequestParam(value = "lastDate") String lastDate, @RequestParam(value = "timesheetUsername", required = false) String timesheetUsername) {

        if (StringUtils.isBlank(firstDate) || StringUtils.isBlank(lastDate)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, false, "ATMCMN_INVALID_DATE", "FAILURE", Collections.singletonList("First and Last date must not be empty")));
        }
        try {
            if (StringUtils.isBlank(timesheetUsername)) {
                timesheetUsername = SessionHolder.getUserLoginDetail().getUsername().toString();
            }
            Map<String, Object> timesheetData = applyTimeSheetService.fetchUserTimeSheetData(firstDate, secondDate, thirdDate, fourthDate, fifthDate, sixthDate, lastDate, timesheetUsername);
            return ResponseEntity.ok(new ApiResponse<>(timesheetData, true, "ATMCMN_TIMESHEET_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching weekly timesheet data for user: {}", timesheetUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(null, false, "ATMCMN_FETCH_ERROR", "FAILURE", Collections.singletonList("An unexpected error occurred while fetching timesheet data")));
        }
    }

    @PostMapping("/timesheetAction")
    public ResponseEntity<ApiResponse<List<TimesheetDTO>>> timeSheetActions(@Valid @RequestBody List<TimesheetDTO> timesheetDtos) {
        try {
            List<TimesheetDTO> timesheetDTOS = applyTimeSheetService.applyTimesheet(timesheetDtos);

            ApiResponse<List<TimesheetDTO>> response = new ApiResponse<>(
                    timesheetDTOS,
                    true,
                    "TIMESHEET-SUBMIT-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            ApiResponse<List<TimesheetDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "TIMESHEET-SUBMIT-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}