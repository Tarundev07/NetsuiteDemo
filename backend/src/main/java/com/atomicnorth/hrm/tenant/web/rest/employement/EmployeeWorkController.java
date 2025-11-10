package com.atomicnorth.hrm.tenant.web.rest.employement;

import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeWorkHistDTO;
import com.atomicnorth.hrm.tenant.service.employement.EmployeeWorkService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/employeeWork")
public class EmployeeWorkController {

    private final Logger log = LoggerFactory.getLogger(EmployeeWorkController.class);
    @Autowired
    private EmployeeWorkService employeeWorkService;


    @PostMapping("/saveOrUpdateEmployeeWork")
    public ResponseEntity<ApiResponse<List<EmployeeWorkHistDTO>>> saveOrUpdateWorkHistory(@Valid @RequestBody List<EmployeeWorkHistDTO> employeeWorkHistDTOs) {
        try {
            List<EmployeeWorkHistDTO> updatedRecords = employeeWorkService.saveOrUpdate(employeeWorkHistDTOs);
            return ResponseEntity.ok(new ApiResponse<>(updatedRecords, true, "EMPLOYEE_WORK_HISTORY_SAVED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error saving or updating work history", e);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_WORK_HISTORY_SAVE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/getByUsername/{userId}")
    public ResponseEntity<ApiResponse<List<EmployeeWorkHistDTO>>> getWorkHistoryByUsername(@PathVariable Integer userId) {
        try {
            List<EmployeeWorkHistDTO> workHistoryList = employeeWorkService.getFamilyByUsername(userId);
            if (!workHistoryList.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(workHistoryList, true, "EMPLOYEE_WORK_HISTORY_FETCHED", "SUCCESS"));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(null, false, "EMPLOYEE_WORK_HISTORY_NOT_FOUND", "FAILURE", Collections.singletonList("No work history found for user ID: " + userId)));
            }
        } catch (Exception e) {
            log.error("Error fetching work history by username", e);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_WORK_HISTORY_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/get/{employmentHistoryId}")
    public ResponseEntity<ApiResponse<EmployeeWorkHistDTO>> getWorkHistoryById(@PathVariable Integer employmentHistoryId) {
        try {
            EmployeeWorkHistDTO workHistory = employeeWorkService.getEducationById(employmentHistoryId);
            if (workHistory != null) {
                return ResponseEntity.ok(new ApiResponse<>(workHistory, true, "EMPLOYEE_WORK_HISTORY_FETCHED", "SUCCESS"));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(null, false, "EMPLOYEE_WORK_HISTORY_NOT_FOUND", "FAILURE", Collections.singletonList("No work history found with ID: " + employmentHistoryId)));
            }
        } catch (Exception e) {
            log.error("Error fetching work history by ID", e);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_WORK_HISTORY_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @PatchMapping("/deactivate/{employmentHistoryId}")
    public ResponseEntity<ApiResponse<String>> deactivateEmployeeWorkHistory(@PathVariable Integer employmentHistoryId) {
        try {
            employeeWorkService.deactivateEmployeeWorkHistory(employmentHistoryId);
            return ResponseEntity.ok(new ApiResponse<>("Work history deactivated successfully.", true, "EMPLOYEE_WORK_HISTORY_DEACTIVATED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error deactivating work history", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null, false, "EMPLOYEE_WORK_HISTORY_DEACTIVATE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }
}
