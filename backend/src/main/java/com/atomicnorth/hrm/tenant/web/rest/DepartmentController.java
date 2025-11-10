package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.domain.Department;
import com.atomicnorth.hrm.tenant.repository.DepartmentRepository;
import com.atomicnorth.hrm.tenant.service.DepartmentService;
import com.atomicnorth.hrm.tenant.service.dto.DepartmentDTO;
import com.atomicnorth.hrm.tenant.service.dto.DepartmentIdNameDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/department")
@EnableTransactionManagement
public class DepartmentController {

    private final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseEntity<ApiResponse<?>> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage()
                ));
        return new ResponseEntity<>(
                new ApiResponse<>(null, false, "ATMCMN_VALIDATION_ERROR", "FAILURE",
                        new ArrayList<>(errors.values())),
                HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> saveOrUpdateDepartment(
            @Valid @RequestBody DepartmentDTO departmentDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        Long id = departmentDTO.getId();
        try {
            if (id == null) {
                // Create operation
                if (departmentRepository.findByDnameIgnoreCaseAndCompany(departmentDTO.getDname(), departmentDTO.getCompany()).isPresent()) {
                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_DUPLICATE", "FAILURE",
                                    Collections.singletonList("This department already exists; consider updating it instead of creating a duplicate: " + departmentDTO.getDname())),
                            HttpStatus.CONFLICT);
                }
                DepartmentDTO savedDepartment = departmentService.createDepartment(departmentDTO);
                return new ResponseEntity<>(
                        new ApiResponse<>(savedDepartment, true, "ATMCMN_DEPARTMENT_CREATED", "SUCCESS"),
                        HttpStatus.CREATED);

            } else {
                // Update operation
                Optional<Department> department = departmentRepository.findByDnameIgnoreCaseAndCompany(
                        departmentDTO.getDname(), departmentDTO.getCompany());
                if (department.isPresent() && !department.get().getId().equals(id)) {
                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_DUPLICATE", "FAILURE",
                                    Collections.singletonList("A Department with department name: '" + departmentDTO.getDname()
                                            + "' already exists for the selected company.")),
                            HttpStatus.CONFLICT);
                }
                DepartmentDTO updatedDepartment = departmentService.updateDepartment(id, departmentDTO);
                return ResponseEntity.ok(
                        new ApiResponse<>(updatedDepartment, true, "ATMCMN_DEPARTMENT_UPDATED", "SUCCESS"));
            }
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_NOT_FOUND", "FAILURE",
                            Collections.singletonList("Department not found with ID: " + id)),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while saving/updating Department", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllLeaveAllocations(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {

        try {
            List<DepartmentDTO> departments = departmentService.findAllDepartments();

            Comparator<DepartmentDTO> defaultComparator = Comparator.comparing(DepartmentDTO::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            if ("desc".equalsIgnoreCase(sortDir)) {
                defaultComparator = defaultComparator.reversed();
            }
            departments.sort(defaultComparator);

            if (searchKeyword != null && !searchKeyword.isEmpty() && searchField != null && !searchField.isEmpty()) {
                if ("isActive".equalsIgnoreCase(searchField)) {
                    if ("active".contains(searchKeyword )) {
                        searchKeyword = String.valueOf(true);
                    } else if ("inactive".contains(searchKeyword )) {
                        searchKeyword = String.valueOf(false);
                    }
                }
                String lowerCaseKeyword = searchKeyword.toLowerCase();
                departments = departments.stream()
                        .filter(department -> {
                            switch (searchField.toLowerCase()) {
                                case "dname":
                                    return department.getDname() != null && department.getDname().toLowerCase().contains(lowerCaseKeyword);
                                case "parentdepartmentname":
                                    return department.getParentDepartmentName() != null && department.getParentDepartmentName().toLowerCase().contains(lowerCaseKeyword);
                                case "payrollcostcentername":
                                    return department.getPayrollCostCenterName() != null && department.getPayrollCostCenterName().toLowerCase().contains(lowerCaseKeyword);
                                case "description":
                                    return department.getDescription() != null && department.getDescription().toLowerCase().contains(lowerCaseKeyword);
                                case "companyname":
                                    return department.getCompanyName() != null && department.getCompanyName().toLowerCase().contains(lowerCaseKeyword);
                                case "isgroup":
                                    return department.getIsGroup() != null && department.getIsGroup().toString().toLowerCase().contains(lowerCaseKeyword);
                                case "leaveblock":
                                    return department.getLeaveBlock() != null && department.getLeaveBlock().toString().toLowerCase().contains(lowerCaseKeyword);
                                case "createdby":
                                    return department.getCreatedBy() != null && department.getCreatedBy().toLowerCase().contains(lowerCaseKeyword);
                                case "updatedby":
                                    return department.getUpdatedBy() != null && department.getUpdatedBy().toLowerCase().contains(lowerCaseKeyword);
                                case "isactive":
                                    return department.getIsActive() != null && department.getIsActive().toString().toLowerCase().contains(lowerCaseKeyword);
                                case "leaveblockname":
                                    return department.getLeaveBlockName() != null && department.getLeaveBlockName().toLowerCase().contains(lowerCaseKeyword);
                                default:
                                    return false;
                            }
                        })
                        .collect(Collectors.toList());
            }

            Map<String, Comparator<DepartmentDTO>> sortingFields = new HashMap<>();
            sortingFields.put("dname", Comparator.comparing(DepartmentDTO::getDname, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("parentdepartmentname", Comparator.comparing(DepartmentDTO::getParentDepartmentName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("payrollcostcentername", Comparator.comparing(DepartmentDTO::getPayrollCostCenterName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("description", Comparator.comparing(DepartmentDTO::getDescription, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("companyname", Comparator.comparing(DepartmentDTO::getCompanyName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("isgroup", Comparator.comparing(d -> d.getIsGroup() != null ? d.getIsGroup().toString() : "", Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("leaveblock", Comparator.comparing(d -> d.getLeaveBlock() != null ? d.getLeaveBlock().toString() : "", Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("createdby", Comparator.comparing(DepartmentDTO::getCreatedBy, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("updatedby", Comparator.comparing(DepartmentDTO::getUpdatedBy, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("isactive", Comparator.comparing(d -> d.getIsActive() != null ? d.getIsActive().toString() : "", Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("leaveblockname", Comparator.comparing(DepartmentDTO::getLeaveBlockName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            sortingFields.put("createddate", Comparator.comparing(DepartmentDTO::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())));

            //Apply requested sorting
            if (!"id".equalsIgnoreCase(sortBy)) {  // ID sorting is already applied by default
                Comparator<DepartmentDTO> comparator = sortingFields.get(sortBy.toLowerCase());
                if (comparator != null) {
                    if ("desc".equalsIgnoreCase(sortDir)) {
                        comparator = comparator.reversed();
                    }
                    departments.sort(comparator);
                }
            }

            //Pagination logic
            int startIndex = (page - 1) * size;
            startIndex = Math.max(0, Math.min(startIndex, departments.size()));
            int endIndex = Math.min(startIndex + size, departments.size());
            List<DepartmentDTO> currentPageData = departments.subList(startIndex, endIndex);

            //Pagination info
            ObjectNode paginationInfo = objectMapper.createObjectNode();
            paginationInfo.put("totalElements", departments.size());
            paginationInfo.put("totalPages", (int) Math.ceil((double) departments.size() / size));
            paginationInfo.put("pageSize", size);
            paginationInfo.put("currentPage", page);

            //Build response
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("departments", objectMapper.valueToTree(currentPageData));
            responseData.set("paginationData", paginationInfo);

            return ResponseEntity.ok(new ApiResponse<>(responseData, true, "ATMCMN_DEPARTMENTS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Departments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_FETCH_ERROR", "FAILURE",
                            Collections.singletonList("An unexpected error occurred while fetching department data")));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentById(@PathVariable Long id) {
        try {
            return departmentService.getDepartmentById(id)
                    .map(dept -> ResponseEntity.ok(new ApiResponse<>(dept, true, "ATMCMN_DEPARTMENT_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_NOT_FOUND", "FAILURE",
                                    Collections.singletonList("Department not found with ID: " + id)),
                            HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error occurred while fetching Department by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartmentById(id);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "ATMCMN_DEPARTMENT_DELETED", "SUCCESS"));
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_NOT_FOUND", "FAILURE",
                            Collections.singletonList("Department not found with ID: " + id)),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while deleting Department", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_DEPARTMENT_DELETE_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/departmentNames")
    public ResponseEntity<ApiResponse<List<DepartmentIdNameDTO>>> getAllDepartmentNames() {
        try {
            List<DepartmentIdNameDTO> departmentIdNameDTOS = departmentService.getAllDname();
            if (departmentIdNameDTOS.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "DEPARTMENT_NAME_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No departments found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(departmentIdNameDTOS, true, "DEPARTMENT_NAME_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching department names", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "DEPARTMENT_NAME_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

}