package com.atomicnorth.hrm.tenant.web.rest.employement;

import com.atomicnorth.hrm.tenant.service.dto.EmployeeEducationHistoryDTO;
import com.atomicnorth.hrm.tenant.service.employement.EmployeeEducationHistService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/employeeEducationHist")
public class EmployeeEducationHistController {
    private final Logger log = LoggerFactory.getLogger(EmployeeEducationHistController.class);
    @Autowired
    private EmployeeEducationHistService employeeEducationHistService;

    @Transactional
    @PostMapping("/saveOrUpdate")
    public ResponseEntity<ApiResponse<List<EmployeeEducationHistoryDTO>>> saveOrUpdateEmployeeEducation(@Valid @RequestBody List<EmployeeEducationHistoryDTO> employeeEducationHistoryDTOs) {
        try {
            List<EmployeeEducationHistoryDTO> updatedRecords = employeeEducationHistService.saveOrUpdate(employeeEducationHistoryDTOs);

            return ResponseEntity.ok(new ApiResponse<>(updatedRecords, true, "EMPLOYEE_EDUCATION_SAVE_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_EDUCATION_SAVE_FAILURE", "ERROR", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/get/{userId}")
    public ResponseEntity<ApiResponse<List<EmployeeEducationHistoryDTO>>> getEducationByUsername(@PathVariable("userId") Integer userId) {
        try {
            List<EmployeeEducationHistoryDTO> educationHistoryList = employeeEducationHistService.getEducationByUsername(userId);

            if (educationHistoryList.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(null, false, "EMPLOYEE_EDUCATION_NOT_FOUND", "FAILURE", Collections.singletonList("No education history found for user ID: " + userId)));
            }
            return ResponseEntity.ok(new ApiResponse<>(educationHistoryList, true, "EMPLOYEE_EDUCATION_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error fetching education history for user ID: " + userId, e);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_EDUCATION_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/geEempEducationId/{empEducationId}")
    public ResponseEntity<ApiResponse<EmployeeEducationHistoryDTO>> getEducationById(@PathVariable("empEducationId") Integer empEducationId) {
        try {
            EmployeeEducationHistoryDTO educationHistoryDTO = employeeEducationHistService.getEducationById(empEducationId);

            if (educationHistoryDTO == null) {
                return ResponseEntity.ok(new ApiResponse<>(null, false, "EMPLOYEE_EDUCATION_NOT_FOUND", "FAILURE", Collections.singletonList("No education history found with ID: " + empEducationId)));
            }

            return ResponseEntity.ok(new ApiResponse<>(educationHistoryDTO, true, "EMPLOYEE_EDUCATION_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error fetching education history for ID: " + empEducationId, e);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_EDUCATION_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @PatchMapping("/deactivate/{empEducationId}")
    public ResponseEntity<ApiResponse<EmployeeEducationHistoryDTO>> deactivateEducation(@PathVariable("empEducationId") Integer empEducationId) {
        try {
            employeeEducationHistService.deactivateEducation(empEducationId);

            return ResponseEntity.ok(new ApiResponse<>(null, true, "EMPLOYEE_EDUCATION_DEACTIVATED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error deactivating education history for ID: " + empEducationId, e);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_EDUCATION_DEACTIVATED_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }
}
