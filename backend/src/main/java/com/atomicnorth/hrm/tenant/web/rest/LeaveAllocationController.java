package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.domain.LeaveAllocation;
import com.atomicnorth.hrm.tenant.domain.branch.LeaveTypes;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveAllocationRepository;
import com.atomicnorth.hrm.tenant.repository.LeaveTypeRepository;
import com.atomicnorth.hrm.tenant.service.LeaveAllocationService;
import com.atomicnorth.hrm.tenant.service.dto.LeaveAllocationDTO;
import com.atomicnorth.hrm.tenant.service.dto.LeaveAllocationDetailsDTO;
import com.atomicnorth.hrm.tenant.service.dto.LeaveAllocationRequestDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave-allocations")
@EnableTransactionManagement
public class LeaveAllocationController {

    private final Logger log = LoggerFactory.getLogger(LeaveAllocationController.class);

    @Autowired
    private LeaveAllocationService leaveAllocationService;

    @Autowired
    private LeaveAllocationRepository leaveAllocationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private ResponseEntity<ApiResponse<?>> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage()
                ));
        return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_VALIDATION_ERROR", "FAILURE",
                new ArrayList<>(errors.values())), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllLeaveAllocations(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
        log.debug("REST request to get paginated Leave Allocations record");
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> leaveAllocationMap = leaveAllocationService.getPaginatedLeaveAllocations(pageable, searchField, searchKeyword,sortBy,sortDir);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    leaveAllocationMap,
                    true,
                    "LEAVE-ALLOCATIONS-FETCHED",
                    "SUCCESS"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching leave allocations", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "LEAVE-ALLOCATION-FETCH-ERROR",
                    "FAILURE",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveAllocationDTO>> getLeaveAllocationById(@PathVariable Long id) {
        try {
            return leaveAllocationService.getLeaveAllocationById(id)
                    .map(leaveAllocation -> ResponseEntity.ok(new ApiResponse<>(leaveAllocation, true, "ATMCMN_LEAVE_ALLOCATION_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_LEAVE_ALLOCATION_NOT_FOUND", "FAILURE",
                            Collections.singletonList("Leave Allocation not found with ID: " + id)), HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error occurred while fetching Leave Allocation by ID", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_LEAVE_ALLOCATION_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<?>> saveLeaveAllocationWithDetails(
            @Valid @RequestBody LeaveAllocationRequestDTO leaveAllocationDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }

        try {
            Integer empId = leaveAllocationDTO.getEmpId();

            if (leaveAllocationDTO.getLeaveDetails() != null) {
                LeaveAllocationDetailsDTO detail = leaveAllocationDTO.getLeaveDetails();

                Integer maxLeave = leaveTypeRepository.findByLeaveCode(detail.getLeaveCode())
                        .map(LeaveTypes::getMaximumLeave)
                        .orElse(null);

                if (maxLeave != null && detail.getLeaveAllocationNumber() != null &&
                        detail.getLeaveAllocationNumber() > maxLeave) {
                    String leaveTypeName = leaveTypeRepository.findByLeaveCode(detail.getLeaveCode())
                            .map(LeaveTypes::getLeaveName)
                            .orElse("Unknown Leave Type");

                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_LEAVE_ALLOCATION_ERROR", "FAILURE",
                                    Collections.singletonList("Leave Allocation for '" + leaveTypeName +
                                            "' can't be greater than the max limit: " + maxLeave)),
                            HttpStatus.OK
                    );
                }

                if (leaveAllocationDTO.getId() == null) {
                    Optional<LeaveAllocation> existing = leaveAllocationRepository
                            .findByEmpIdAndIsActiveAndLeaveAllocationDetails_LeaveCode(
                                    empId, "A", detail.getLeaveCode());

                    if (existing.isPresent()) {
                        String employeeName = employeeRepository.findEmployeeFullNameById(Long.valueOf(empId))
                                .orElse(String.valueOf(empId));
                        String leaveTypeName = leaveTypeRepository.findByLeaveCode(detail.getLeaveCode())
                                .map(LeaveTypes::getLeaveName)
                                .orElse(detail.getLeaveCode());

                        return new ResponseEntity<>(
                                new ApiResponse<>(null, false, "ATMCMN_LEAVE_ALLOCATION_EXISTS", "FAILURE",
                                        Collections.singletonList("Leave Allocation already exists for employee '" + employeeName +
                                                "' and leave type '" + leaveTypeName + "'")),
                                HttpStatus.OK
                        );
                    }
                }
            }

            LeaveAllocationDTO savedAllocation = leaveAllocationService.createOrUpdateLeaveAllocation(leaveAllocationDTO);
            String responseCode = (leaveAllocationDTO.getId() == null)
                    ? "LEAVE_ALLOCATION_CREATED"
                    : "LEAVE_ALLOCATION_UPDATED";

            return new ResponseEntity<>(
                    new ApiResponse<>(savedAllocation, true, responseCode, "SUCCESS"),
                    HttpStatus.CREATED
            );

        } catch (Exception e) {
            log.error("Error occurred while saving Leave Allocation", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_LEAVE_ALLOCATION_SAVE_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK
            );
        }
    }

    @GetMapping("/check-allocation/{empId}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmployeeLeaveAllocationExists(@PathVariable Integer empId) {
        boolean exists = leaveAllocationRepository.existsByEmpId(empId);
        if (exists) {
            String employeeName = employeeRepository.findEmployeeFullNameById(empId.longValue())
                    .orElse(String.valueOf(empId));
            return ResponseEntity.ok(new ApiResponse<>(true, false, "ATMCMN_LEAVE_ALLOCATION_EXISTS", "FAILURE",
                    Collections.singletonList("Leave Allocation already exists for employee '" + employeeName + "'. Please select another employee.")));
        }
        return ResponseEntity.ok(new ApiResponse<>(false, true, "NO_EXISTING_ALLOCATION", "SUCCESS"));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponse<LeaveAllocationDTO>> getLeaveAllocationByEmpId(@PathVariable Integer empId) {
        try {
            return leaveAllocationService.getLeaveAllocationByEmpId(empId)
                    .map(dto -> ResponseEntity.ok(new ApiResponse<>(dto, true, "ATMCMN_LEAVE_ALLOCATION_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_LEAVE_ALLOCATION_NOT_FOUND", "FAILURE",
                            Collections.singletonList("Leave Allocation not found for EMP_ID: " + empId)), HttpStatus.OK));
        } catch (Exception e) {
            log.error("Error occurred while fetching Leave Allocation by EMP_ID", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_LEAVE_ALLOCATION_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("saveLeaveAllocation")
    public ResponseEntity<ApiResponse<LeaveAllocation>> saveLeaveAllocation(@Valid @RequestBody LeaveAllocationRequestDTO leaveAllocationRequestDTO) {
        try {
            LeaveAllocation leaveAllocation = leaveAllocationService.saveLeaveAllocation(leaveAllocationRequestDTO);
            return ResponseEntity.ok(new ApiResponse<>(leaveAllocation, true, "LEAVE_ALLOCATION_SAVED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "LEAVE_ALLOCATION_SAVED_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @DeleteMapping("deleteById/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteById(@PathVariable("id") Long id, @RequestParam(value = "status") String status) {
        try {
            if (!StringUtils.hasText(status)) {
                return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "LEAVE_ALLOCATION_DELETED_FAILED", "FAILURE", Collections.singletonList("Status is mandatory.")));
            }
            LeaveAllocation leaveAllocation = leaveAllocationService.deleteById(id, status);
            return ResponseEntity.ok(new ApiResponse<>(leaveAllocation, true, "LEAVE_ALLOCATION_DELETED_SUCCESS", "SUCCESS"));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "LEAVE_ALLOCATION_DELETED_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

}
