package com.atomicnorth.hrm.tenant.web.rest.leave;

import com.atomicnorth.hrm.tenant.domain.LeaveRequest;
import com.atomicnorth.hrm.tenant.service.dto.leave.LeaveRequestDTO;
import com.atomicnorth.hrm.tenant.service.leave.ApplyLeaveService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
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
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
public class ApplyLeaveController {

    @Autowired
    private ApplyLeaveService leaveService;

    @GetMapping("generateLeaveReqNo")
    public ResponseEntity<ApiResponse<String>> generateLeaveRequestNumber(@RequestParam(value = "empId", required = false) Integer empId) {
        try {
            String requestNumber = leaveService.getRequestNumber(empId);
            return ResponseEntity.ok(new ApiResponse<>(requestNumber, true, "REQUEST_NUMBER_GENERATED", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "REQUEST_NUMBER_GENERATION_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @PostMapping("apply-leave")
    public ResponseEntity<ApiResponse<Object>> applyLeave(@Valid @RequestBody LeaveRequestDTO leaveRequestDTO) {
        try {
            LeaveRequest leaveRequest = leaveService.applyLeave(leaveRequestDTO);
            return ResponseEntity.ok(new ApiResponse<>(leaveRequest, true, "LEAVE_APPLIED_SUCCESSFULLY", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "LEAVE_APPLIED_FAILED", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("leaveBalance")
    public ResponseEntity<ApiResponse<Object>> leaveBalance(@RequestParam(value = "empId", required = false) Integer empId) {
        try {
            Map<String, Double> leaveBalance = leaveService.getLeaveBalance(empId);
            return ResponseEntity.ok(new ApiResponse<>(leaveBalance, true, "LEAVE_BALANCE_PER_LEAVE_CODE_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "LEAVE_BALANCE_PER_LEAVE_CODE_FETCHED_FAILURE", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }

    @GetMapping("getLedger")
    public HttpEntity<ApiResponse<Map<String, Object>>> ledgerDetails(
            @RequestParam(value = "empId", required = false) Integer empId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "ledgerId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> ledgerDetails = leaveService.getLedgerDetails(empId, sortBy, sortDir, searchField, searchKeyword, pageable);
            return ResponseEntity.ok(new ApiResponse<>(ledgerDetails, true, "LEAVE_LEDGER_DETAILS_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "LEAVE_LEDGER_DETAILS_FETCHED_FAILURE", "FAILURE", Collections.singletonList(e.getMessage())));
        }
    }
}
