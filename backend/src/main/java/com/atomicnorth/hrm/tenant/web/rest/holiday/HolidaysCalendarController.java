package com.atomicnorth.hrm.tenant.web.rest.holiday;

import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendar;
import com.atomicnorth.hrm.tenant.repository.holiday.HolidaysCalendarRepository;
import com.atomicnorth.hrm.tenant.service.dto.holiday.HolidaysCalendarSaveRequest;
import com.atomicnorth.hrm.tenant.service.dto.holiday.HolidaysCalendarSaveResponse;
import com.atomicnorth.hrm.tenant.service.holiday.HolidaysCalendarService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.atomicnorth.hrm.util.commonClass.PaginatedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/holiday-calendars")
@Tag(name = "Holiday Calendar", description = "Operations related to Holiday Calendar")
public class HolidaysCalendarController {

    private final HolidaysCalendarService holidaysCalendarService;

    private final HolidaysCalendarRepository holidaysCalendarRepository;

    public HolidaysCalendarController(HolidaysCalendarService holidaysCalendarService, HolidaysCalendarRepository holidaysCalendarRepository) {
        this.holidaysCalendarService = holidaysCalendarService;
        this.holidaysCalendarRepository = holidaysCalendarRepository;
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<HolidaysCalendarSaveResponse>> saveHolidaysCalendar(@Valid @RequestBody HolidaysCalendarSaveRequest request) {
        try {
            HolidaysCalendarSaveResponse responseHolidayCalendar = holidaysCalendarService.saveHolidaysCalendar(request);
            ApiResponse<HolidaysCalendarSaveResponse> response = new ApiResponse<>(
                    responseHolidayCalendar,
                    true,
                    "HOLIDAY-CREATED-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<HolidaysCalendarSaveResponse> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HOLIDAY-CREATED-FAILURE",
                    "Warning",
                    List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (ConstraintViolationException ex) {
            // Specific handling for constraint violations, like duplicate entries
            ApiResponse<HolidaysCalendarSaveResponse> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HOLIDAY-CREATED-FAILURE",
                    "Error",
                    List.of("Duplicate entry detected: " + ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception ex) {
            // Generic exception handler
            ApiResponse<HolidaysCalendarSaveResponse> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HOLIDAY-CREATED-FAILURE",
                    "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getHolidayCalendarById")
    public ResponseEntity<ApiResponse<HolidaysCalendarSaveResponse>> getHolidayCalendarById(
            @RequestParam(name = "holidayId") Integer holidayId) {
        // Fetch the holiday calendar by name
        HolidaysCalendarSaveResponse responseDataAllHoliday = holidaysCalendarService.getHolidayCalendarByHolidayId(holidayId);
        try {
            // BranchDto createdBranchMessage = branchService.createBranch(branchDto);
            ApiResponse<HolidaysCalendarSaveResponse> response = new ApiResponse<>(
                    responseDataAllHoliday,
                    true,
                    "HOLIDAY-CREATED-SUCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<HolidaysCalendarSaveResponse> errorResponse = new ApiResponse<>(
                    responseDataAllHoliday,
                    false,
                    "HOLIDAY-CREATED-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<HolidaysCalendarSaveResponse> errorResponse = new ApiResponse<>(
                    responseDataAllHoliday,
                    false,
                    "HOLIDAY-CREATED-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /*UPDATE API*/
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<HolidaysCalendarSaveResponse>> updateHolidaysCalendar(
            @RequestParam String name,
            @RequestBody HolidaysCalendarSaveRequest request) {
        try {
            HolidaysCalendarSaveResponse response = holidaysCalendarService.updateHolidaysCalendar(name, request);
            ApiResponse<HolidaysCalendarSaveResponse> apiResponse = new ApiResponse<>(response, true, "HOLIDAY-UPDATED-SUCCESS", "Information");
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException ex) {
            ApiResponse<HolidaysCalendarSaveResponse> errorResponse = new ApiResponse<>(null, false, "HOLIDAY-UPDATE-FAILURE", "Warning", List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<HolidaysCalendarSaveResponse> errorResponse = new ApiResponse<>(null, false, "HOLIDAY-UPDATE-FAILURE", "Error", List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getAllHolidayCalendars")
    public ResponseEntity<ApiResponse<PaginatedResponse<HolidaysCalendarSaveResponse>>> getAllHolidayCalendars(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            // Fetch paginated response from service
            PaginatedResponse<HolidaysCalendarSaveResponse> paginatedResponse =
                    holidaysCalendarService.getHolidayCalendarData(pageNumber, pageSize, searchColumn, searchValue, sortBy, sortDir);

            // Prepare API response
            ApiResponse<PaginatedResponse<HolidaysCalendarSaveResponse>> response = new ApiResponse<>(
                    paginatedResponse, true, "HOLIDAY-FETCHED-SUCCESS", "Information"
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<PaginatedResponse<HolidaysCalendarSaveResponse>> errorResponse = new ApiResponse<>(
                    null, false, "HOLIDAY-FETCH-FAILURE", "Error", List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<ApiResponse<HolidaysCalendarSaveResponse>> saveOrUpdateHolidaysCalendarNew(@Valid @RequestBody HolidaysCalendarSaveRequest request) {
        try {
            if (request.getHolidayCalendarId() == null) {
                Optional<HolidaysCalendar> existingCalendarName = holidaysCalendarRepository.findByName(request.getName().trim());
                if (existingCalendarName.isPresent()) {
                    ApiResponse<HolidaysCalendarSaveResponse> existingBranchResponse = new ApiResponse<>(
                            null,
                            false,
                            "CALENDAR-ALREADY-EXISTS",
                            "Warning",
                            Collections.singletonList(
                                    "Calendar with name " + request.getName() + " already exists.")
                    );
                    return ResponseEntity.status(HttpStatus.OK).body(existingBranchResponse); // 409 Conflict
                }
            }
            HolidaysCalendarSaveResponse responseHolidayCalendar = holidaysCalendarService.saveOrUpdateHolidaysCalendarNew(request);

            ApiResponse<HolidaysCalendarSaveResponse> response = new ApiResponse<>(
                    responseHolidayCalendar,
                    true,
                    "HOLIDAY-SAVE-OR-UPDATE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException ex) {
            ApiResponse<HolidaysCalendarSaveResponse> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HOLIDAY-SAVE-OR-UPDATE-FAILURE",
                    "Warning",
                    List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/findHolidayIdAndName")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHolidayNameAndId() {
        try {
            List<Map<String, Object>> employees = this.holidaysCalendarService.findHolidayNameAndId();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "HOLIDAY_DETAILS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No Holiday details found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "HOLIDAY_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "HOLIDAY_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("/getCalendarById")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHolidayCalendarDateById(
            @RequestParam(value = "calendarId") Integer calendarId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false, defaultValue = "holidayDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> responseData =
                    holidaysCalendarService.getHolidayCalendarDateById(calendarId, pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    responseData, true, "CALENDAR-FETCHED-SUCCESS", "Information");

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "ONBOARDING-FETCH-FAILURE", "Error", List.of(ex.getMessage()));

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
}

