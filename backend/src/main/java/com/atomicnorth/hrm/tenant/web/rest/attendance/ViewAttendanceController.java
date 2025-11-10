package com.atomicnorth.hrm.tenant.web.rest.attendance;

import com.atomicnorth.hrm.tenant.service.attendance.ViewAttendanceService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/view")
public class ViewAttendanceController {
    @Autowired
    private ViewAttendanceService viewAttendanceService;

    @GetMapping("fetchAttendance")
    public HttpEntity<ApiResponse<Map<String, Object>>> fetchAttendance(
            @RequestParam(value = "employeeId", required = false) Integer empId,
            @RequestParam(value = "firstDate") String firstDate,
            @RequestParam(value = "lastDate") String lastDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "formRfNum", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {
        try {
            if (!StringUtils.hasText(firstDate) || !StringUtils.hasText(lastDate)) {
                return new ResponseEntity<>(new ApiResponse<>(null, false, "ATTENDANCE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList("First Date and Last Date are mandatory.")), HttpStatus.BAD_REQUEST);
            }
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> attendanceMoafList = viewAttendanceService.fetchAttendance(empId, firstDate, lastDate, pageable, searchField, searchKeyword, sortBy, sortDir);
            return ResponseEntity.ok(new ApiResponse<>(attendanceMoafList, true, "ATTENDANCE_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (ParseException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATTENDANCE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList("Invalid date format. Please use YYYY-MM-DD.")), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATTENDANCE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}
