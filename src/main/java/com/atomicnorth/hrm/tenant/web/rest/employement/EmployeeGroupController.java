package com.atomicnorth.hrm.tenant.web.rest.employement;

import com.atomicnorth.hrm.tenant.domain.Group;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.GroupRepo;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeGroupDTO;
import com.atomicnorth.hrm.tenant.service.employement.EmployeeGroupService;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@ControllerAdvice
@RequestMapping("/api/empGroups")
public class EmployeeGroupController {

    @Autowired
    private EmployeeGroupService employeeGroupService;

    @Autowired
    private GroupRepo groupRepo;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeGroupDTO>> createEmployeeGroup(@Valid @RequestBody EmployeeGroupDTO empGrpDTO) {
        try {
            UserLoginDetail userDetail =SessionHolder.getUserLoginDetail();
            Group existingGroupName = groupRepo.findByGroupNameIgnoreCase(empGrpDTO.getGroupName());
            if (existingGroupName != null) {
                ApiResponse<EmployeeGroupDTO> existingEmployeeGrpRes = new ApiResponse<>(
                        null,
                        false,
                        "EMP-GROUP-ALREADY-EXISTS",
                        "Warning",
                        Collections.singletonList(
                                "Employee Group name " + empGrpDTO.getGroupName() + " already exists.")
                );
                return ResponseEntity.status(HttpStatus.OK).body(existingEmployeeGrpRes);
            }
            EmployeeGroupDTO createEmpGrp = employeeGroupService.createEmpGrp(empGrpDTO);

            ApiResponse<EmployeeGroupDTO> response = new ApiResponse<>(
                    createEmpGrp,
                    true,
                    "EMP-GROUP-CREATED-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            ApiResponse<EmployeeGroupDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMP-GROUP-CREATED-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeGroupDTO>> updateEmployeeGroup(@PathVariable Long id, @RequestBody EmployeeGroupDTO empGrpDTO) {
        try {
            EmployeeGroupDTO updateEmpGrp = employeeGroupService.updateEmpGrp(empGrpDTO, id);

            ApiResponse<EmployeeGroupDTO> response = new ApiResponse<>(
                    updateEmpGrp,
                    true,
                    "EMP-GROUP-CREATED-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            ApiResponse<EmployeeGroupDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMP-GROUP-CREATED-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGroupList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchColumn", required = false) String searchColumn,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> GROUPDTOList = employeeGroupService.getPaginatedEmployeeGroups(pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    GROUPDTOList,
                    true,
                    "GROUP-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "GROUP-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "GROUP-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeGroupList(@RequestParam String groupId) {
        try {
            Map<String, Object> GROUPDTOList = employeeGroupService.getEmployeeIdsByGroupId(Long.parseLong(groupId));
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    GROUPDTOList,
                    true,
                    "EMPLOYEE-GROUP-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-GROUP-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-GROUP-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

}
