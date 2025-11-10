package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.service.LeaveTrackService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-track")
public class LeaveTrackController {

    private final Logger log = LoggerFactory.getLogger(LeaveTrackController.class);

    @Autowired
    private LeaveTrackService leaveTrackService;

    @GetMapping("/getAllTrackLeaves")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllTrackLeaves(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "leaveRfNum") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "empId", required = false) Integer empId
    ) {
        log.debug("REST request to get paginated track leave data");
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> leaveMap = leaveTrackService.getPaginatedTrackLeavesByEmpId(empId, pageable, searchField, searchKeyword);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    leaveMap,
                    true,
                    "TRACK-LEAVES-FETCHED",
                    "SUCCESS"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching track leave data", e);
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "TRACK-LEAVES-FETCH-ERROR",
                    "FAILURE",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
}