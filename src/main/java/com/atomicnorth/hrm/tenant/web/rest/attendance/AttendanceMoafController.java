package com.atomicnorth.hrm.tenant.web.rest.attendance;

import com.atomicnorth.hrm.tenant.domain.attendance.AttendanceMoaf;
import com.atomicnorth.hrm.tenant.service.attendance.AttendanceMoafService;
import com.atomicnorth.hrm.tenant.service.dto.attendance.AttendanceMoafDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequestMapping("/api/moaf")
@RestController
public class AttendanceMoafController {

    @Autowired
    private AttendanceMoafService attendanceMoafService;

    @GetMapping("/getUsersPendingMOAFRequest")
    public ResponseEntity<ApiResponse<PaginatedResponse<AttendanceMoafDTO>>> getUsersPendingMOAFRequestNew(
            @RequestParam(required = false) String status,
            @RequestParam(required = true) LocalDate firstDate,
            @RequestParam(required = true) LocalDate lastDate,
            @RequestParam(required = false) Set<Integer> divisions,
            @RequestParam(required = false) Set<Long> department,
            @RequestParam(required = false) Set<Integer> reportingManagers,
            @RequestParam(required = false) Set<Integer> employeesIds,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String searchColumn,  // New parameter
            @RequestParam(required = false) String searchValue   // New parameter
    ) {
        try {
            Date startDate = java.sql.Date.valueOf(firstDate);
            LocalDateTime lastDateTime = lastDate.atTime(LocalTime.MAX);
            Date endDate = Date.from(lastDateTime.atZone(ZoneId.systemDefault()).toInstant());
            PaginatedResponse<AttendanceMoafDTO> paginatedMoafData = attendanceMoafService.fetchUserMoafPendingRequest(
                    status, firstDate, lastDate, divisions, department, reportingManagers, employeesIds,
                    page, size, sortBy, sortOrder, searchColumn, searchValue
            );

            ApiResponse<PaginatedResponse<AttendanceMoafDTO>> response = new ApiResponse<>(
                    paginatedMoafData, true, "PENDING-MOAF-REQUESTS-RETRIEVED-SUCCESSFULLY", "Information"
            );

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ApiResponse<PaginatedResponse<AttendanceMoafDTO>> errorResponse = new ApiResponse<>(
                    null, false, "PENDING-MOAF-REQUESTS-RETRIEVAL-FAILED", "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/updateMOAFData")
    public ResponseEntity<ApiResponse<String>> approveBulkMOAFData(
            @RequestParam(value = "moafids", required = true) Integer[] moafids,
            @RequestParam(value = "status", required = true) String status,
            @RequestParam(value = "requestNumbers", required = true) String[] requestNumbers) {
        try {
            String message = attendanceMoafService.updateMOAFRequest(moafids, status,requestNumbers);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(message, true, "MOAF_UPDATED_SUCCESS", "Success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "MOAF_UPDATED_ERROR", "Error", Collections.singletonList("Error update moaf list.")));
        }
    }

    @PostMapping("/applyMoaf")
    public ResponseEntity<ApiResponse<AttendanceMoafDTO>> applyMoaf(@Valid @RequestBody AttendanceMoafDTO attendanceMoafDTO) {
        try {
            if (attendanceMoafDTO.getFormRfNum() == null) {
                Optional<AttendanceMoaf> existingMoaf = attendanceMoafService.findAttendanceExistingRecords(attendanceMoafDTO);
                if (existingMoaf.isPresent()) {
                    ApiResponse<AttendanceMoafDTO> existingBranchResponse = new ApiResponse<>(
                            attendanceMoafDTO,
                            false,
                            "MOAF-ALREADY-EXISTS",
                            "Warning",
                            Collections.singletonList("Moaf date " + attendanceMoafDTO.getMoafDate() + " already exists.")
                    );
                    return ResponseEntity.status(HttpStatus.OK).body(existingBranchResponse);
                }
            }
            AttendanceMoafDTO attendanceMoaf = attendanceMoafService.saveUpdateMoaf(attendanceMoafDTO);
            String responseCode = attendanceMoafDTO.getFormRfNum() == null
                    ? "MOAF-CREATED-SUCCESS"
                    : "MOAF-UPDATED-SUCCESS";
            ApiResponse<AttendanceMoafDTO> response = new ApiResponse<>(
                    attendanceMoaf,
                    true,
                    responseCode,
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            ApiResponse<AttendanceMoafDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "MOAF-CREATED-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getMoafDetails/{id}")
    public ResponseEntity<ApiResponse<Object>> getAllMoafDetails(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "formRfNum", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            if ("employeeName".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "employee.firstName");
            } else if ("employeeNumber".equals(sortBy)) {
                sort = Sort.by(Sort.Direction.fromString(sortDir), "employee.employeeNumber");
            }
            Pageable pageable = PageRequest.of(page -1, size, sort);
            Map<String, Object> employeeMaofDetails = attendanceMoafService.getAllMoafDetails(pageable, searchField, searchKeyword, id);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(employeeMaofDetails, true, "MOAF_LIST_FETCHED_SUCCESS", "Success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "MOAF_LIST_FETCHED_ERROR", "Error", Collections.singletonList("Error fetching moaf list.")));
        }
    }

    @GetMapping("/getMoafDetailsByFormRfNum/{formRfNum}")
    public ResponseEntity<ApiResponse<Object>> getMoafDetailsByFormRfNum(@PathVariable Integer formRfNum) {
        try {
            Map<String, Object> employeeMaofDetails = attendanceMoafService.getMoafDetailsByFormRfNum(formRfNum);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(employeeMaofDetails, true, "MOAF_LIST_FETCHED_SUCCESS", "Success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "MOAF_LIST_FETCHED_ERROR", "Error", Collections.singletonList("Error fetching moaf list.")));
        }
    }
}