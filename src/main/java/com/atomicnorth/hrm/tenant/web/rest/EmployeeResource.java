package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.service.EmployeeService;
import com.atomicnorth.hrm.tenant.service.EmployeeTemplateService;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeDTO;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeHierarchyViewDTO;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeProfileDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.AddEmployee;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeBasicDetails;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeContactInfoDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeePersonalInfoDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.EmployeeWorkInfoDTO;
import com.atomicnorth.hrm.tenant.service.dto.employement.Response.EmployeeResponseDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee")
public class EmployeeResource {

    private final Logger log = LoggerFactory.getLogger(EmployeeResource.class);
    private final EmployeeService employeeService;
    private final EmployeeTemplateService templateService;

    public EmployeeResource(EmployeeService employeeService, EmployeeTemplateService templateService) {
        this.employeeService = employeeService;
        this.templateService = templateService;
    }

    private ResponseEntity<ApiResponse<?>> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
        return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_VALIDATION_ERROR", "FAILURE", new ArrayList<>(errors.values())), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> saveEmployee(@Valid @RequestBody EmployeeResponseDTO employeeResponseDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        try {
            EmployeeResponseDTO savedEmployeeResponseDTO = employeeService.addEmployee(employeeResponseDTO, true);
            return new ResponseEntity<>(new ApiResponse<>(savedEmployeeResponseDTO, true, "ATMCMN_EMPLOYEE_CREATED", "SUCCESS"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while saving Employee", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_SAVE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/personal-info")
    public ResponseEntity<ApiResponse<?>> updatePersonalInfo(@PathVariable Integer id, @RequestBody EmployeePersonalInfoDTO dto) {
        try {
            EmployeeResponseDTO savedEmployeeResponseDTO = employeeService.updatePersonalInfo(id, dto);
            return new ResponseEntity<>(new ApiResponse<>(savedEmployeeResponseDTO, true, "ATMCMN_EMPLOYEE_PERSONAL_INFO", "SUCCESS"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while saving Employee", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_PERSONAL_INFO_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/work-info")
    public ResponseEntity<ApiResponse<?>> updateWorkInfo(@PathVariable Integer id, @RequestBody EmployeeWorkInfoDTO dto) {
        try {
            EmployeeResponseDTO savedEmployeeResponseDTO = employeeService.updateWorkInfo(id, dto);
            return new ResponseEntity<>(new ApiResponse<>(savedEmployeeResponseDTO, true, "ATMCMN_EMPLOYEE_PERSONAL_INFO", "SUCCESS"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while saving Employee", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_PERSONAL_INFO_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/contact-info")
    public ResponseEntity<ApiResponse<?>> updateContactInfo(@PathVariable Integer id, @RequestBody EmployeeContactInfoDTO dto) {
        try {
            EmployeeResponseDTO savedEmployeeResponseDTO = employeeService.updateContactInfo(id, dto);
            return new ResponseEntity<>(new ApiResponse<>(savedEmployeeResponseDTO, true, "ATMCMN_EMPLOYEE_PERSONAL_INFO", "SUCCESS"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while saving Employee", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_PERSONAL_INFO_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(@PathVariable("employeeId") Integer employeeId) {
        try {
            return employeeService.getEmployeeById(employeeId).map(employeeResponseDTO -> ResponseEntity.ok(new ApiResponse<>(employeeResponseDTO, true, "ATMCMN_EMPLOYEE_FETCHED", "SUCCESS"))).orElse(new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_NOT_FOUND", "FAILURE", Collections.singletonList("Employee not found with ID: " + employeeId)), HttpStatus.OK));
        } catch (Exception e) {
            log.error("Error occurred while fetching Employee by ID", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_FETCH_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Sort getSort(String sortBy, String sortDir) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir.toUpperCase());

        switch (sortBy) {
            case "fullName":
                return Sort.by(direction, "firstName")
                        .and(Sort.by(direction, "middleName"))
                        .and(Sort.by(direction, "lastName"));
            case "designationName":
                return Sort.by(direction, "designation.designationName");
            case "departmentName":
                return Sort.by(direction, "department.dname");
            default:
                return Sort.by(direction, sortBy);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmpList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "employeeId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchColumn", required = false) String searchColumn,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        try {
            Sort sort = getSort(sortBy, sortDir);
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Map<String, Object> EmployeeResponseDTO = employeeService.getPaginatedEmployee(pageable, searchColumn, searchValue);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(EmployeeResponseDTO,
                    true, "EMP-LIST-SUCCESS", "Success");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(null, false,
                    "EMP-LIST-FAILURE", "Warning", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "EMP-LIST-FAILURE", "Error",
                    Collections.singletonList(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<EmployeeBasicDetails>>> getEmployeesDetails() {
        try {
            List<EmployeeBasicDetails> employees = employeeService.getAllEmployeesWithDetails();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "EMPLOYEE_DETAILS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No employee details found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "EMPLOYEE_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching employee details", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "EMPLOYEE_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAllManager")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeeRMName() {
        try {
            List<EmployeeDTO> employeeResponseDTOS = employeeService.findManagerFullName();
            if (employeeResponseDTOS == null || employeeResponseDTOS.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "NO-DATA-FOUND", "No manager records found."),
                        HttpStatus.NOT_FOUND
                );
            }
            return new ResponseEntity<>(
                    new ApiResponse<>(employeeResponseDTOS, true, "RM-DETAILS-FETCHED", "Success"),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ERROR", "An error occurred while fetching manager details."),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> getEmployeeProfile(@RequestParam Integer empId) {
        try {
            Optional<EmployeeProfileDTO> employeeProfile = employeeService.getEmployeeProfileById(empId);

            return employeeProfile.map(employeeProfileDTO -> ResponseEntity.ok(new ApiResponse<>(
                    employeeProfileDTO, true, "EMPLOYEE-PROFILE-FETCHED", "Success"))).orElseGet(() -> ResponseEntity.ok(new ApiResponse<>(null, false,
                    "EMPLOYEE_NOT_FOUND", "FAILURE")));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(null, false, "ERROR", "An error occurred while fetching employee profile.")
            );
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<Employee>>> getActiveEmployeesByDepartment(
            @RequestParam("departmentId") List<Long> departmentIds) {
        try {
            if (departmentIds == null || departmentIds.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(null, false, "NO_DEPARTMENT_IDS_PROVIDED", "FAILURE",
                                Collections.singletonList("At least one departmentId is required."))
                );
            }

            List<Employee> employees = employeeService.getActiveEmployeesByDepartment(departmentIds, "Y");
            return ResponseEntity.ok(new ApiResponse<>(employees, true, "EMPLOYEES_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching filtered employees", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "EMPLOYEES_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allManager")
    public ResponseEntity<ApiResponse<List<EmployeeBasicDetails>>> getAllManager(@RequestParam(value = "roleType", required = false) String roleType) {
        try {
            List<EmployeeBasicDetails> managerDetails = employeeService.getAllManager(roleType);
            return ResponseEntity.ok(new ApiResponse<>(managerDetails, true, "MANAGER_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "MANAGER_DETAILS_FETCHED", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download-employee-template")
    public ResponseEntity<byte[]> downloadTemplate() throws Exception {
        byte[] report = templateService.generateTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=EmployeeTemplate.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(report);
    }

    @GetMapping("filters")
    public ResponseEntity<ApiResponse<List<EmployeeHierarchyViewDTO>>> applyFilters(
            @RequestParam(name = "divisionId", required = false) List<Integer> divisionId,
            @RequestParam(name = "departmentId", required = false) List<Integer> departmentId,
            @RequestParam(name = "reportingManagerId", required = false) List<Integer> reportingManagerId
    ) {
        try {
            List<Integer> divisions = divisionId != null ? divisionId : Collections.emptyList();
            List<Integer> departments = departmentId != null ? departmentId : Collections.emptyList();
            List<Integer> rms = reportingManagerId != null ? reportingManagerId : Collections.emptyList();
            List<EmployeeHierarchyViewDTO> results = employeeService.applyFilters(divisions, departments, rms);
            return ResponseEntity.ok(new ApiResponse<>(results, true, "FILTERED_EMPLOYEES_FETCH_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "FILTERED_EMPLOYEES_FETCH_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("/add-employees")
    public ResponseEntity<ApiResponse<?>> saveEmployee(@Valid @RequestBody List<AddEmployee> addEmployees) {
        try {
            List<EmployeeResponseDTO> savedEmployeeResponseDTO = employeeService.addEmployees(addEmployees);
            return new ResponseEntity<>(new ApiResponse<>(savedEmployeeResponseDTO, true, "ATMCMN_EMPLOYEE_CREATED", "SUCCESS"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while saving Employee", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_EMPLOYEE_SAVE_ERROR", "FAILURE", Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}