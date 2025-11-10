package com.atomicnorth.hrm.tenant.web.rest.attendance;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.attendance.SupraShiftEntity;
import com.atomicnorth.hrm.tenant.service.attendance.ShiftAssignmentsServices;
import com.atomicnorth.hrm.tenant.service.dto.attendance.ShiftEmployeeDTO;
import com.atomicnorth.hrm.tenant.service.dto.attendance.SupraShiftDTO;
import com.atomicnorth.hrm.tenant.service.dto.attendance.SupraShiftDetailsDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.Response.EmployeeResponseDTO;
import com.atomicnorth.hrm.util.OldApiResponseMessage;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/shift-assignment")
@EnableTransactionManagement
public class ShiftAssignmentResource {

    @Autowired
    private ShiftAssignmentsServices shiftAssignmentsServices;
    @PostMapping("/assignShift-saveOrUpdate")
    public ResponseEntity<ApiResponse<ShiftEmployeeDTO>> saveOrUpdateCustomer(@Valid @RequestBody ShiftEmployeeDTO dto) {
        try {
            boolean isUpdate = dto.getShiftEmpId() != null;
            ShiftEmployeeDTO savedDto = shiftAssignmentsServices.saveOrUpdate(dto);
            String message = isUpdate ? "Shift Assign updated successfully" : "Shift Assign  created successfully";
            String code = isUpdate ? "UPDATE-SUCCESS" : "CREATE-SUCCESS";
            ApiResponse<ShiftEmployeeDTO> response = new ApiResponse<>(savedDto, true, code, message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<ShiftEmployeeDTO> errorResponse = new ApiResponse<>(null, false, "SAVE-OR-UPDATE-FAILED", "Error while saving/updating customer", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
    @GetMapping("/getAllUser")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUser() {
        try {
            List<Map<String, Object>> employeeData = shiftAssignmentsServices.getAllUsers();

            if (employeeData.isEmpty()) {
                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(employeeData, false, "NO_USERS_FOUND", "Warning");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(employeeData, true, "USER_FETCH_SUCCESS", "Information");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ResourceNotFoundException ex) {
            ApiResponse<List<Map<String, Object>>> errorResponse = new ApiResponse<>(Collections.emptyList(), false, "USER_FETCH_FAILURE", "Warning", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);

        } catch (Exception ex) {
            ApiResponse<List<Map<String, Object>>> errorResponse = new ApiResponse<>(Collections.emptyList(), false, "USER_FETCH_FAILURE", "Error", Collections.singletonList("An unexpected error occurred."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getSingleUserDetails")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getUserById(@RequestParam String username) {
        try {
            List<EmployeeResponseDTO> employeeData = shiftAssignmentsServices.getUserDetails(Integer.parseInt(username));

            // Check if employee data exists
            if (employeeData.isEmpty()) {
                ApiResponse<List<EmployeeResponseDTO>> response = new ApiResponse<>(Collections.emptyList(), false, "USER_NOT_FOUND", "Warning", Collections.singletonList("Employee not found with this username: " + username));
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            ApiResponse<List<EmployeeResponseDTO>> response = new ApiResponse<>(employeeData, true, "USER_FETCH_SUCCESS", "Information");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (NumberFormatException ex) {
            ApiResponse<List<EmployeeResponseDTO>> errorResponse = new ApiResponse<>(Collections.emptyList(), false, "INVALID_USERNAME_FORMAT", "Error", Collections.singletonList("Invalid username format. It must be a number."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);

        } catch (ResourceNotFoundException ex) {
            ApiResponse<List<EmployeeResponseDTO>> errorResponse = new ApiResponse<>(Collections.emptyList(), false, "USER_NOT_FOUND", "Warning", Collections.singletonList("Employee not found with this username: " + username));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);

        } catch (Exception ex) {
            ApiResponse<List<EmployeeResponseDTO>> errorResponse = new ApiResponse<>(Collections.emptyList(), false, "USER_FETCH_FAILURE", "Error", Collections.singletonList("An unexpected error occurred while fetching the employee."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getAllActiveUser")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllActiveUser() {
        try {
            List<Map<String, Object>> activeUsers = shiftAssignmentsServices.getAllActiveUser();

            if (activeUsers.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(activeUsers, false, "NO_ACTIVE_USERS_FOUND", "Warning"));
            }
            return ResponseEntity.ok(new ApiResponse<>(activeUsers, true, "ACTIVE_USERS_FETCH_SUCCESS", "Information"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(Collections.emptyList(), false, "ACTIVE_USERS_FETCH_FAILURE", "Error", Collections.singletonList("Unexpected error occurred")));
        }
    }

    @GetMapping("/getDataAddShift")
    public ResponseEntity<ApiResponse<List<SupraShiftEntity>>> getEmployeeSupraM06Shift() {
        List<SupraShiftEntity> employeeData = shiftAssignmentsServices.getEmployeeSupraM06Shift();

        try {
            // BranchDto createdBranchMessage = branchService.createBranch(branchDto);
            ApiResponse<List<SupraShiftEntity>> response = new ApiResponse<>(employeeData, true, "DATA_ADD_SHIFT-CREATED-SUCCESS", "Information");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<List<SupraShiftEntity>> errorResponse = new ApiResponse<>(employeeData, false, "DATA_ADD_SHIFT-CREATED-FAILURE", "Warning", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<List<SupraShiftEntity>> errorResponse = new ApiResponse<>(employeeData, false, "DATA_ADD_SHIFT-CREATED-FAILURE", "Error", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }

    }

    @GetMapping("/getShiftDetails/{shiftId}")
    public ResponseEntity<Object> getEmployeeById(@PathVariable Integer shiftId) {
        try {
            List<SupraShiftDetailsDTO> employeeData = shiftAssignmentsServices.getShiftDetail(shiftId);

            if (employeeData != null) {
                return new ResponseEntity<>(employeeData, HttpStatus.OK);
            } else {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder().message("Employee not found with this id: " + employeeData).status(HttpStatus.OK).success(false).build();
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            }
        } catch (ResourceNotFoundException ex) {
            // Handle the case where the employee is not found
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder().message("Employee not found with this id: " + shiftId).status(HttpStatus.OK).success(false).build();
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            // Handle other exceptions
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder().message("An error occurred while fetching the employee.").status(HttpStatus.OK).success(false).build();
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }
    }

    @DeleteMapping("/deleteAssignShift/{shiftEmployeeId}")
    public ResponseEntity<ApiResponse<String>> deleteAssignShift(@PathVariable Integer shiftEmployeeId) {
        try {
            shiftAssignmentsServices.delete(shiftEmployeeId);

            ApiResponse<String> response = new ApiResponse<>("Shift with ID " + shiftEmployeeId + " has been deleted successfully.", true, "SHIFT_DELETION_SUCCESS", "Information");

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ResourceNotFoundException ex) {
            ApiResponse<String> errorResponse = new ApiResponse<>(null, false, "SHIFT_DELETION_FAILURE", "Warning", Collections.singletonList("Shift not found with ID: " + shiftEmployeeId));

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);

        } catch (Exception ex) {
            ApiResponse<String> errorResponse = new ApiResponse<>(null, false, "SHIFT_DELETION_FAILURE", "Error", Collections.singletonList("An error occurred while deleting the shift."));

            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/fetchUserShiftAssignHistory")
    public ResponseEntity<ApiResponse<Object>> fetchUserShiftAssignHistory(
            @RequestParam(value = "employeeId") Integer employeeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "shiftEmpId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword",required = false) String searchKeyword) {
        try {
            if (employeeId == null) {
                return new ResponseEntity<>(new ApiResponse<>(null, false, "EMPLOYEE_ID_IS_MANDATORY", "ERROR", Collections.singletonList("Employee Id is mandatory.")), HttpStatus.BAD_REQUEST);
            }
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> resultMap = shiftAssignmentsServices.fetchUserShiftAssignment(employeeId, sortBy, sortDir, searchField, searchKeyword, pageable);
            ApiResponse<Object> response = new ApiResponse<>(resultMap, true, "SHIFT_ASSIGN_HISTORY_FETCH_SUCCESS", "SUCCESS");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<Object> errorResponse = new ApiResponse<>(null, false, "SHIFT_ASSIGN_HISTORY_FETCH_FAILURE", "ERROR", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
    @GetMapping("/getByUsername/{username}")
    public ResponseEntity<List<Map<String, Object>>> getByUsername(@PathVariable Integer username) {
        List<Map<String, Object>> result = shiftAssignmentsServices.getShiftDetailsByUsername(username);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/fetchAvailableShiftList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> fetchAvailableShiftList() {
        try {
            List<Map<String, Object>> shiftList = new LinkedList<>();
            List<Object[]> activeShiftList = shiftAssignmentsServices.activeShifts();
            if (activeShiftList != null && !activeShiftList.isEmpty()) {
                for (Object[] shift : activeShiftList) {
                    Map<String, Object> shiftMap = new HashMap<>();
                    shiftMap.put("SHIFT_ID", shift[0]);
                    shiftMap.put("CALENDAR_ID", shift[1]);
                    shiftMap.put("SHIFT_CODE", shift[2]);
                    shiftMap.put("NAME", shift[3]);
                    shiftMap.put("DESCRIPTION", shift[4]);
                    shiftMap.put("IS_DEFAULT", shift[5]);
                    shiftMap.put("WEEK_OFFS", shift[6]);
                    shiftList.add(shiftMap);
                }
                ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(shiftList, true, "AVAILABLE_SHIFT_LIST_FETCH_SUCCESS", "Information", null);

                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                ApiResponse<List<Map<String, Object>>> noDataResponse = new ApiResponse<>(new LinkedList<>(), false, "AVAILABLE_SHIFT_LIST_NOT_FOUND", "Warning", Collections.singletonList("No available shifts found"));

                return ResponseEntity.status(HttpStatus.OK).body(noDataResponse);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ApiResponse<List<Map<String, Object>>> errorResponse = new ApiResponse<>(new LinkedList<>(), false, "AVAILABLE_SHIFT_LIST_FETCH_FAILURE", "Error", Collections.singletonList("An error occurred while fetching the available shift list."));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/saveOrUpdateMasterShift")
    public ResponseEntity<ApiResponse<SupraShiftDTO>> saveOrUpdateShift(@Valid @RequestBody SupraShiftDTO dto) {
        try {
            boolean isUpdate = dto.getShiftId() != null;
            SupraShiftDTO savedDto = shiftAssignmentsServices.saveOrUpdateShift(dto);
            String message = isUpdate ? "Shift updated successfully" : "Shift created successfully";
            String code = isUpdate ? "SHIFT-UPDATE-SUCCESS" : "SHIFT-CREATE-SUCCESS";
            ApiResponse<SupraShiftDTO> response = new ApiResponse<>(savedDto, true, code, message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            // Handle duplicate shift name or other validation errors
            ApiResponse<SupraShiftDTO> errorResponse = new ApiResponse<>(null, false, "SHIFT-VALIDATION-FAILED", ex.getMessage(), Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);

        } catch (Exception ex) {
            ApiResponse<SupraShiftDTO> errorResponse = new ApiResponse<>(null, false, "SHIFT-SAVE-OR-UPDATE-FAILED", "Error while saving/updating shift", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getById/{shiftId}")
    public ResponseEntity<ApiResponse<SupraShiftDTO>> getByShiftId(@PathVariable Integer shiftId) {
        try {
            SupraShiftDTO dto = shiftAssignmentsServices.getShiftById(shiftId);
            return ResponseEntity.ok(new ApiResponse<>(dto, true, "FETCH-SUCCESS", "Shift fetched successfully"));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "NOT-FOUND", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "ERROR", "An error occurred", List.of(ex.getMessage())));
        }
    }

    @GetMapping("/getAllShifts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllShifts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "shiftId") String sortBy, @RequestParam(defaultValue = "desc") String sortDir, @RequestParam(required = false) String searchField, @RequestParam(required = false) String searchKeyword) {
        try {
            Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page - 1, size, sort);

            Map<String, Object> response = shiftAssignmentsServices.getAllShifts(pageable, searchField, searchKeyword);

            return ResponseEntity.ok(new ApiResponse<>(response, true, "FETCH-SUCCESS", "Shift list fetched successfully"));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "INVALID-FIELD", "Invalid search field", List.of(ex.getMessage())));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, false, "ERROR", "An error occurred", List.of(ex.getMessage())));
        }
    }

    @GetMapping("getActiveShifts")
    public ResponseEntity<ApiResponse<List<SupraShiftDTO>>> getActiveShifts() {
        try {
            List<SupraShiftDTO> activeShifts = shiftAssignmentsServices.getActiveShifts();
            return ResponseEntity.ok(new ApiResponse<>(activeShifts, true, "ACTIVE_SHIFT_FETCH_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ACTIVE_SHIFT_FETCH_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}
