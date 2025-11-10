package com.atomicnorth.hrm.tenant.web.rest.employeeExit;

import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitApproval;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitApprovalDTO;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitRequestDTO;
import com.atomicnorth.hrm.tenant.service.employeeExit.EmpExitRequestService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/exits")
public class EmpExitRequestController {

    private final Logger log = LoggerFactory.getLogger(EmpExitRequestController.class);

    @Autowired
    private EmpExitRequestService exitService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmpExitRequestDTO>> saveorUpdateEmployeeExit(@RequestBody EmpExitRequestDTO dto) {
        try {
            boolean isNew = dto.getId() == null;
            EmpExitRequestDTO saved = exitService.saveorUpdateEmployeeExit(dto);
            return ResponseEntity.ok(new ApiResponse<>(
                    saved,
                    true,
                    isNew ? "CREATED_SUCCESS" : "UPDATED_SUCCESS",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    null,
                    false,
                    "SAVE_OR_UPDATE_FAILED",
                    "FAILURE",
                    Collections.singletonList(e.getMessage())
            ));
        }
    }

    @GetMapping("/form/{employeeId}")
    public ResponseEntity<ApiResponse<EmpExitRequestDTO>> getExitFormInitialData(@PathVariable("employeeId") Integer employeeId) {
        try {
            EmpExitRequestDTO dto = exitService.getExitFormData(employeeId);
            if (dto != null) {
                return ResponseEntity.ok(new ApiResponse<>(
                        dto,
                        true,
                        "INITIAL_DATA_FETCHED_SUCCESS",
                        "SUCCESS"
                ));
            }
            return ResponseEntity.ok(new ApiResponse<>(
                    null,
                    true,
                    "INITIAL_DATA_FETCHED_SUCCESS",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    null,
                    false,
                    "INITIAL_DATA_FETCH_ERROR",
                    "FAILURE",
                    Collections.singletonList(e.getMessage())
            ));
        }
    }

    @GetMapping("/getApprovalsListByEmployeeId/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApprovalsListByEmployeeId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchValue", required = false) String searchValue,
            @RequestParam(value = "searchColumn", required = false) String searchColumn
    ) {
        try {
            ApiResponse<Map<String, Object>> approvalsResponse =
                    exitService.getApprovals(id, searchColumn, searchValue, sortBy, sortDir, page - 1, size);

            return ResponseEntity.ok(approvalsResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "APPROVAL_LIST_FETCHED_ERROR", "Error",
                            Collections.singletonList("Error fetching approval list: " + e.getMessage())));
        }
    }


    @GetMapping("/getExitById/{id}")
    public ResponseEntity<ApiResponse<EmpExitApprovalDTO>> getEmployeeExitById(@PathVariable("id") Integer id) {
        try {
            EmpExitApprovalDTO dto = exitService.getEmployeeExitById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    dto,
                    true,
                    "FETCHED_SUCCESS",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    null,
                    false,
                    "FETCH_ERROR",
                    "FAILURE",
                    Collections.singletonList(e.getMessage())
            ));
        }
    }

    @PatchMapping("/review/{id}")
    public ResponseEntity<ApiResponse<EmpExitApprovalDTO>> reviewExitRequest(
            @PathVariable("id") Integer id,
            @RequestBody EmpExitApprovalDTO requestDto) {
        try {
            EmpExitApprovalDTO dto = exitService.reviewExitRequest(id, requestDto);
            return ResponseEntity.ok(new ApiResponse<>(
                    dto,
                    true,
                    "REVIEW_SUCCESS",
                    "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    null,
                    false,
                    "REVIEW_ERROR",
                    "FAILURE",
                    Collections.singletonList(e.getMessage())
            ));
        }
    }

}