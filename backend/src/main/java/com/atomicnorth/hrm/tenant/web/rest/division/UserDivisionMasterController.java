package com.atomicnorth.hrm.tenant.web.rest.division;

import com.atomicnorth.hrm.tenant.service.division.UserDivisionMasterService;
import com.atomicnorth.hrm.tenant.service.dto.division.UserDivisionMasterDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-division-master")
public class UserDivisionMasterController {

    private final UserDivisionMasterService userDivisionMasterService;

    public UserDivisionMasterController(UserDivisionMasterService userDivisionMasterService) {
        this.userDivisionMasterService = userDivisionMasterService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<UserDivisionMasterDTO>> saveOrUpdateDivision(
            @Valid @RequestBody UserDivisionMasterDTO divisionDTO) {

        try {
            UserDivisionMasterDTO savedDto = userDivisionMasterService.saveOrUpdate(divisionDTO);
            ApiResponse<UserDivisionMasterDTO> response = new ApiResponse<>(
                    savedDto,
                    true,
                    "DIVISION-SAVE-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<UserDivisionMasterDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DIVISION-SAVE-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);

        } catch (Exception ex) {
            ApiResponse<UserDivisionMasterDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DIVISION-SAVE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/getAllUserDivisionsData")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUserDivisionsData(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "divisionId") String sortColumn,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDirection), sortColumn);
            Map<String, Object> divisions = userDivisionMasterService.getAllUserDivisions(pageable, searchColumn, searchValue, sortColumn, sortDirection);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    divisions, true, "DIVISIONS-RETRIEVED-SUCCESS", "Information"
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "DIVISIONS-RETRIEVED-FAILURE", "Error", List.of(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/division/{id}")
    public ResponseEntity<ApiResponse<UserDivisionMasterDTO>> getUserDivisionById(@PathVariable Long id) {
        try {
            UserDivisionMasterDTO dto = userDivisionMasterService.getUserDivisionById(id);

            ApiResponse<UserDivisionMasterDTO> response = new ApiResponse<>(
                    dto, true, "DIVISION-FOUND", "Information"
            );
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<UserDivisionMasterDTO> response = new ApiResponse<>(
                    null, false, "DIVISION-NOT-FOUND", "Error", List.of(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/findDivisionIdAndName")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmpGradeNameAndId() {
        try {
            List<Map<String, Object>> employees = this.userDivisionMasterService.findDivisionIdAndName();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "EMP_DIVISION_DETAILS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No Division details found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "EMP_DIVISION_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "EMP_DIVISION_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }
}