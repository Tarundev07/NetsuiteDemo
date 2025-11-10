package com.atomicnorth.hrm.tenant.web.rest.leave;

import com.atomicnorth.hrm.tenant.domain.LeaveRequest;
import com.atomicnorth.hrm.tenant.service.dto.leave.LeaveRequestDTO;
import com.atomicnorth.hrm.tenant.service.leave.LeaveApprovalService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.DateTimeException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-approval")
public class LeaveApprovalController {

    @Autowired
    private LeaveApprovalService leaveApprovalService;

    @GetMapping("getLeaveRequestList")
    public ResponseEntity<ApiResponse<Object>> getLeaveRequestList(
            @RequestParam(value = "employeeId", required = false) Integer employeeId,
            @RequestParam(value = "status") String status,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "leaveRfNum", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {
        try {
            if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
                return new ResponseEntity<>(new ApiResponse<>(null, false, "LEAVE_REQUEST_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList("Start and End Dates are mandatory.")), HttpStatus.BAD_REQUEST);
            }
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> leaveRequestList = leaveApprovalService.getLeaveRequestList(employeeId, status, startDate, endDate, sortBy, sortDir, searchField, searchKeyword, pageable);
            return ResponseEntity.ok(new ApiResponse<>(leaveRequestList, true, "LEAVE_REQUEST_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (DateTimeException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "LEAVE_REQUEST_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList("Invalid date format. Please use YYYY-MM-DD.")), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "LEAVE_REQUEST_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("updateLeaveRequest")
    public ResponseEntity<ApiResponse<Object>> updateLeaveRequest(@Valid @RequestBody LeaveRequestDTO leaveRequestDTO) {
        try {
            if (!StringUtils.hasText(leaveRequestDTO.getApproveFlag())) {
                return new ResponseEntity<>(new ApiResponse<>(null, false, "UPDATE_LEAVE_REQUEST_FAILED", "ERROR", Collections.singletonList("Approve Flag is mandatory.")), HttpStatus.BAD_REQUEST);
            }
            LeaveRequest leaveRequest = leaveApprovalService.updateLeaveRequest(leaveRequestDTO);
            return ResponseEntity.ok(new ApiResponse<>(leaveRequest, true, "UPDATE_LEAVE_REQUEST_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "UPDATE_LEAVE_REQUEST_FAILED", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("leaveReversalRequest")
    public ResponseEntity<ApiResponse<LeaveRequest>> leaveReversal(@RequestParam(value = "leaveRfNum") Long leaveRfNum) {
        try {
            LeaveRequest leaveRequest = leaveApprovalService.leaveReversal(leaveRfNum);
            return ResponseEntity.ok(new ApiResponse<>(leaveRequest, true, "LEAVE_REVERSAL_REQUEST_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "LEAVE_REVERSAL_REQUEST_FAILED", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}
