package com.atomicnorth.hrm.tenant.web.rest.employement;

import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeSkillSetDTO;
import com.atomicnorth.hrm.tenant.service.employement.EmployeeSkillSetService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/employee-skill-set")
public class EmployeeSkillSetResource {

    @Autowired
    private EmployeeSkillSetService employeeSkillSetService;

    @Transactional
    @PostMapping("/create-employee-skill")
    public ResponseEntity<ApiResponse<List<EmployeeSkillSetDTO>>> saveOrUpdateSills(@Valid @RequestBody List<EmployeeSkillSetDTO> employeeSkillSetDTO) {
        try {
            List<EmployeeSkillSetDTO> updatedRecords = employeeSkillSetService.saveOrUpdateSkills(employeeSkillSetDTO);

            return ResponseEntity.ok(new ApiResponse<>(updatedRecords, true, "EMPLOYEE_SKILL_SAVE_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_SKILL_SAVE_FAILURE", "ERROR", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("/get/{employeeId}")
    public ResponseEntity<ApiResponse<List<EmployeeSkillSetDTO>>> getAllSkillsByUsername(@PathVariable Integer employeeId) {
        try {
            List<EmployeeSkillSetDTO> addresses = employeeSkillSetService.getAllAddressesByUsername(employeeId);

            if (addresses.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(null, false, "EMPLOYEE_SKILL_NOT_FOUND", "FAILURE", Collections.singletonList("No Skills found for user ID: " + employeeId)));
            }
            return ResponseEntity.ok(new ApiResponse<>(addresses, true, "EMPLOYEE_SKILL_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "EMPLOYEE_SKILL_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }
}