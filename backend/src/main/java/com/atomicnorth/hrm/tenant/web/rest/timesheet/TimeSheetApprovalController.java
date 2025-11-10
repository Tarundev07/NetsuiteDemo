package com.atomicnorth.hrm.tenant.web.rest.timesheet;

import com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO.UserTimesheetDTO;
import com.atomicnorth.hrm.tenant.service.timesheet.TimeSheetApprovalService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/timesheetApproval")
public class TimeSheetApprovalController {
    private final Logger log = LoggerFactory.getLogger(TimeSheetApprovalController.class);
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TimeSheetApprovalService timeSheetApprovalService;

    @GetMapping("/fetchTimeDataBasedPrjctUser")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTimesheetDataPrjctUser(
            @RequestParam(value = "strtDate") String strtDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "username") String username) {

        try {
            List<Map<String, Object>> timesheetData = timeSheetApprovalService.getDataByPrjctUser(
                    strtDate, endDate, username);

            return ResponseEntity.ok(new ApiResponse<>(timesheetData, true, "ATMCMN_TIMESHEET_FETCHED", "SUCCESS"));

        } catch (DataAccessException e) {
            log.error("Database error while fetching timesheet data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "ATMCMN_DATABASE_ERROR", "FAILURE",
                            Collections.singletonList("Database error occurred while fetching timesheet data")));

        } catch (Exception e) {
            log.error("Unexpected error while fetching timesheet data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "ATMCMN_FETCH_ERROR", "FAILURE",
                            Collections.singletonList("An unexpected error occurred while fetching timesheet data")));
        }
    }

    @PutMapping("/updateTimesheetStatus")
    public ResponseEntity<ApiResponse<Boolean>> updateTimesheetStatus(
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "result") boolean result,
            @RequestParam(value = "appRmrk") String appRmrk) {

        try {
            boolean status = timeSheetApprovalService.updateTimesheetStatusUser(startDate, endDate, result, username, appRmrk);
            if (status && !result) {
                Thread t = new Thread(() -> {
                    try {
                        timeSheetApprovalService.removeSourceTSAttendanceLog(startDate, endDate, username);
                    } catch (Exception e) {
                        log.error("Error while removing attendance log", e);
                    }
                });
                t.start();
            }
            return ResponseEntity.ok(new ApiResponse<>(status, true, "ATMCMN_TIMESHEET_STATUS_UPDATED", "SUCCESS"));
        } catch (DataAccessException e) {
            log.error("Database error while updating timesheet status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, false, "ATMCMN_DATABASE_ERROR", "FAILURE",
                            Collections.singletonList("Database error occurred while updating timesheet status")));

        } catch (Exception e) {
            log.error("Unexpected error while updating timesheet status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, false, "ATMCMN_UPDATE_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/submitBulkTimesheet")
    public ResponseEntity<ApiResponse<ObjectNode>> submitBulkTimesheet(@RequestBody UserTimesheetDTO timesheetBean) {
        try {
            if (timesheetBean.getBulkRequest() == null || timesheetBean.getBulkRequest().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(null, false, "TIMESHEET_ROWS_NULL", "FAILURE", List.of("Timesheet rows cannot be null or empty."))
                );
            }
            String result = timeSheetApprovalService.saveUserTimesheet(timesheetBean);
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.put("message", result);
            return ResponseEntity.ok(
                    new ApiResponse<>(responseData, true, "TIMESHEET_SUBMITTED", "SUCCESS")
            );
        } catch (Exception e) {
            log.error("Error occurred while submitting bulk timesheet", e);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponse<>(null, false, "TIMESHEET_SUBMISSION_ERROR", "FAILURE", List.of(e.getMessage()))
            );
        }
    }

    @GetMapping("/getTimesheetData")
    public ResponseEntity<ApiResponse<ObjectNode>> getTimesheetData(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "startDate", required = true) String startDate,
            @RequestParam(value = "endDate", required = true) String endDate,
            @RequestParam(value = "filterVar", required = false) String filterVar,
            @RequestParam(value = "allReporteeFlag", required = false) String allReporteeFlag,
            @RequestParam(value = "departments", required = false) String departments,
            @RequestParam(value = "divisions", required = false) String divisions,
            @RequestParam(value = "reportingmanagers", required = false) String reportingmanagers,
            @RequestParam(value = "sortBy", defaultValue = "user_name", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // Adjust start and end date to the week range
            LocalDate startDateObj = LocalDate.parse(startDate);
            while (!startDateObj.getDayOfWeek().toString().equalsIgnoreCase("SUNDAY")) {
                startDateObj = startDateObj.minusDays(1);
            }
            LocalDate endDateObj = LocalDate.parse(endDate);
            while (!endDateObj.getDayOfWeek().toString().equalsIgnoreCase("SATURDAY")) {
                endDateObj = endDateObj.plusDays(1);
            }

            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);

            // Fetch paginated data from service
            Page<Map<String, Object>> userPage = timeSheetApprovalService.getUsersDataUnderReportee(
                    userId,
                    startDateObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    endDateObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    filterVar,
                    allReporteeFlag,
                    departments,
                    divisions,
                    reportingmanagers,
                    pageable);

            // Apply search if provided
            List<Map<String, Object>> fullData = userPage.getContent();
            if (searchKeyword != null && !searchKeyword.isEmpty() && searchField != null && !searchField.isEmpty()) {
                fullData = fullData.stream()
                        .filter(data -> data.containsKey(searchField) &&
                                data.get(searchField) != null &&
                                data.get(searchField).toString().toLowerCase().contains(searchKeyword.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Build the response data
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(fullData));
            responseData.put("totalItems", userPage.getTotalElements());
            responseData.put("totalPages", userPage.getTotalPages());
            responseData.put("pageSize", size);
            responseData.put("currentPage", page);

            // Build and return the API response
            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(responseData, true, "TIMESHEET_DATA_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching timesheet data", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(null, false, "TIMESHEET_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("getTSDataByProject")
    public HttpEntity<ApiResponse<Map<String, Object>>> getTSDataByProject(
            @RequestParam(value = "projectId") Integer projectId,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "sortBy", defaultValue = "user_name", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            if (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) {
                return new ResponseEntity<>(new ApiResponse<>(null, false, "TIMESHEET_SUMMARY_FETCHED_ERROR", "ERROR", Collections.singletonList("Start Date and End Date are mandatory.")), HttpStatus.BAD_REQUEST);
            }
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> summaries = timeSheetApprovalService.getTSByProject(projectId, startDate, endDate, status, sortBy, sortDir, searchField, searchKeyword, pageable);
            return ResponseEntity.ok(new ApiResponse<>(summaries, true, "TIMESHEET_SUMMARY_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "TIMESHEET_SUMMARY_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }

    }
}