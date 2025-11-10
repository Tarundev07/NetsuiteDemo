package com.atomicnorth.hrm.tenant.web.rest.employement;

import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeFamilyDTO;
import com.atomicnorth.hrm.tenant.service.employement.EmployeeFamilyService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/employeeFamily")
public class EmployeeFamilyController {
    private final Logger log = LoggerFactory.getLogger(EmployeeFamilyController.class);

    @Autowired
    private EmployeeFamilyService employeeFamilyService;

    @PostMapping("/saveAndUpdateFamily")
    public ResponseEntity<ApiResponse<List<EmployeeFamilyDTO>>> saveOrUpdateEmployeeFamily(@Valid @RequestBody List<EmployeeFamilyDTO> employeeFamilyDTOs) {

        try {
            List<EmployeeFamilyDTO> updatedList = employeeFamilyService.saveOrUpdate(employeeFamilyDTOs);
            ApiResponse<List<EmployeeFamilyDTO>> response = new ApiResponse<>(updatedList, true, "EMPLOYEE-FAMILY-SAVE-SUCCESS", "Success");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<List<EmployeeFamilyDTO>> errorResponse = new ApiResponse<>(null, false, "EMPLOYEE-FAMILY-SAVE-FAILURE", "Warning", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<List<EmployeeFamilyDTO>> errorResponse = new ApiResponse<>(null, false, "EMPLOYEE-FAMILY-SAVE-FAILURE", "Error", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("employeeFamily/{userId}")
    public ResponseEntity<ApiResponse<List<EmployeeFamilyDTO>>> getFamilyByUsername(@PathVariable Integer userId) {
        try {
            List<EmployeeFamilyDTO> familyMembers = employeeFamilyService.getFamilyByUsername(userId);
            ApiResponse<List<EmployeeFamilyDTO>> response = new ApiResponse<>(familyMembers, true, "EMPLOYEE-FAMILY-RETRIEVE-SUCCESS", "Success");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<List<EmployeeFamilyDTO>> errorResponse = new ApiResponse<>(null, false, "EMPLOYEE-FAMILY-RETRIEVE-FAILURE", "Error", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/get/{memberId}")
    public ResponseEntity<ApiResponse<EmployeeFamilyDTO>> getFamilyById(@PathVariable("memberId") Integer memberId) {
        try {
            return Optional.ofNullable(employeeFamilyService.getFamilyById(memberId)).map(employeeFamilyDTO -> ResponseEntity.ok(new ApiResponse<>(employeeFamilyDTO, true, "ATMCMN_EMPLOYEE_FAMILY_FETCHED", "SUCCESS"))).orElse(new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_FAMILY_NOT_FOUND", "FAILURE", Collections.singletonList("Employee Family member not found with ID: " + memberId)), HttpStatus.OK));
        } catch (Exception e) {
            log.error("Error occurred while fetching Employee Family member by ID", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_FAMILY_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}
