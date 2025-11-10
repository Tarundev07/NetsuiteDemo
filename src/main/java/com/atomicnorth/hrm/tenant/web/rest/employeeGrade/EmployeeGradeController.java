package com.atomicnorth.hrm.tenant.web.rest.employeeGrade;

import com.atomicnorth.hrm.tenant.service.dto.employeeGrade.EmployeeGradeDTO;
import com.atomicnorth.hrm.tenant.service.employeeGrade.EmployeeGradeService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/employee-grade")
public class EmployeeGradeController {

    @Autowired
    EmployeeGradeService gradeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeGradeDTO>> saveEmployeeGrade(@Valid @RequestBody EmployeeGradeDTO gradeDTO) {
        EmployeeGradeDTO save = gradeService.saveUpdate(gradeDTO);
        try {
            ApiResponse<EmployeeGradeDTO> response = new ApiResponse<>(
                    save,
                    true,
                    "EMPLOYEE-GRADE-CREATE-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<EmployeeGradeDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-GRADE-CREATE-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<EmployeeGradeDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-GRADE-CREATE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }


    /*Niraj Code*/

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeGradeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(required = false) String searchColumn,  // Added search column
            @RequestParam(required = false) String searchValue    // Added search value
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> gradeDTOList = gradeService.getPaginatedEmployeeGrades(pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    gradeDTOList,
                    true,
                    "EMPLOYEE-GRADE-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-GRADE-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-GRADE-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    /*-----------------end---------------*/
    @PatchMapping("{id}")
    public ResponseEntity<ApiResponse<EmployeeGradeDTO>> updateEmployeeGrade(
            @Valid
            @PathVariable Integer id,
            @RequestBody EmployeeGradeDTO dto
    ) {
        EmployeeGradeDTO employeeGradeDTO = gradeService.updateEmployeeGrade(id, dto);
        try {
            ApiResponse<EmployeeGradeDTO> response = new ApiResponse<>(
                    employeeGradeDTO,
                    true,
                    "EMPLOYEE-GRADE-UPDATE-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<EmployeeGradeDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-GRADE-UPDATE-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<EmployeeGradeDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-GRADE-UPDATE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/findEmpGradeIdAndName")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmpGradeNameAndId() {
        try {
            List<Map<String, Object>> employees = this.gradeService.findEmpGradeNameAndId();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "EMP_GRADE_DETAILS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No Employee Grade details found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "EMP_GRADE_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "EMP_GRADE_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }
}
