package com.atomicnorth.hrm.tenant.web.rest.branch;

import com.atomicnorth.hrm.exception.ResourceNotFoundException;
import com.atomicnorth.hrm.tenant.domain.branch.Branch;
import com.atomicnorth.hrm.tenant.domain.branch.EmployeeAdvance;
import com.atomicnorth.hrm.tenant.domain.branch.HRSettings;
import com.atomicnorth.hrm.tenant.domain.branch.LeaveTypes;
import com.atomicnorth.hrm.tenant.repository.LeaveTypeRepository;
import com.atomicnorth.hrm.tenant.repository.branch.BranchRepository;
import com.atomicnorth.hrm.tenant.repository.branch.HRSettingsRepository;
import com.atomicnorth.hrm.tenant.service.branch.BranchService;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeAdvanceDto;
import com.atomicnorth.hrm.tenant.service.dto.branch.BranchDto;
import com.atomicnorth.hrm.tenant.service.dto.branch.BranchWithAddressDto;
import com.atomicnorth.hrm.tenant.service.dto.branch.HRSettingDto;
import com.atomicnorth.hrm.tenant.service.dto.branch.LeaveTypeDto;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final Logger log = LoggerFactory.getLogger(BranchController.class);
    private final BranchService branchService;
    private final BranchRepository branchRepository;
    private final HRSettingsRepository hrSettingsRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public BranchController(BranchService branchService, BranchRepository branchRepository, HRSettingsRepository hrSettingsRepository, LeaveTypeRepository leaveTypeRepository) {
        this.branchService = branchService;
        this.branchRepository = branchRepository;
        this.hrSettingsRepository = hrSettingsRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @PostMapping("/createOrUpdate")
    public ResponseEntity<ApiResponse<Branch>> createBranch(@Valid @RequestBody BranchDto branchDto) {
        try {
            Optional<Branch> existingBranch = branchRepository.findByCode(branchDto.getCode());
            if (existingBranch.isPresent()) {
                ApiResponse<Branch> existingBranchResponse = new ApiResponse<>(
                        existingBranch.get(),
                        false,
                        "BRANCH-ALREADY-EXISTS",
                        "Warning",
                        Collections.singletonList(
                                "Branch with code " + branchDto.getCode() + " already exists.")
                );
                return ResponseEntity.status(HttpStatus.OK).body(existingBranchResponse); // 409 Conflict
            }
            Branch createdBranchMessage = branchService.createBranch(branchDto);
            ApiResponse<Branch> response = new ApiResponse<>(
                    createdBranchMessage,
                    true,
                    "BRANCH-CREATED-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            ApiResponse<Branch> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "BRANCH-CREATED-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/save-hrms-settings")
    public ResponseEntity<ApiResponse<HRSettings>> createHrmsSettings(@Valid @RequestBody HRSettingDto hrSettingDto) {
        try {
            branchService.validateBirthdays(hrSettingDto.getBirthdays());
            Optional<HRSettings> existingHrSettings = hrSettingsRepository.findByEmployeeId(hrSettingDto.getEmployeeId());
            if (existingHrSettings.isPresent()) {
                ApiResponse<HRSettings> existingBranchResponse = new ApiResponse<>(
                        existingHrSettings.get(),
                        false,
                        "HR-SETTINGS-ALREADY-EXISTS",
                        "Warning",
                        Collections.singletonList("Employee Name " + hrSettingDto.getEmployeeName() + " already exists.")
                );
                return ResponseEntity.status(HttpStatus.OK).body(existingBranchResponse);
            }
            HRSettings hrSettings = branchService.createHrmsSettings(hrSettingDto);
            ApiResponse<HRSettings> response = new ApiResponse<>(
                    hrSettings,
                    true,
                    "HR-SETTINGS-CREATED-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ValidationException ex) {
            ApiResponse<HRSettings> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HR-SETTINGS-VALIDATION-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<HRSettings> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HR-SETTINGS-CREATION-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ObjectNode>> getHrmsSettingsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword
    ) {
        log.debug("REST request to get paginated HRMS settings records");

        try {
            //    Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> hrmsSettingsData = branchService.getHrmsSettingsList();

            List<Map<String, Object>> fullData = ((List<?>) hrmsSettingsData.get("result")).stream()
                    .map(obj -> objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
                    }))
                    .collect(Collectors.toList());

            // Apply Searching
            if (searchField != null && !searchField.isEmpty() && searchKeyword != null && !searchKeyword.isEmpty()) {
                fullData = fullData.stream()
                        .filter(data -> data.containsKey(searchField) &&
                                data.get(searchField) != null &&
                                data.get(searchField).toString().toLowerCase().contains(searchKeyword.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Apply Sorting
            if (sortBy != null && !sortBy.isEmpty()) {
                Comparator<Map<String, Object>> comparator = (data1, data2) -> {
                    Object obj1 = data1.getOrDefault(sortBy, "");
                    Object obj2 = data2.getOrDefault(sortBy, "");
                    if (obj1 == null) obj1 = "";
                    if (obj2 == null) obj2 = "";
                    if (obj1 instanceof Number && obj2 instanceof Number) {
                        return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
                    }
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                };
                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }
                fullData = fullData.stream().sorted(comparator).collect(Collectors.toList());
            }

            // Apply Pagination
            int totalItems = fullData.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = Math.max(0, Math.min((page - 1) * size, totalItems));
            int endIndex = Math.min(startIndex + size, totalItems);
            List<Map<String, Object>> currentPageData = fullData.subList(startIndex, endIndex);

            // Pagination Info
            ObjectNode paginationInfo = objectMapper.createObjectNode();
            paginationInfo.put("totalElements", totalItems);
            paginationInfo.put("totalPages", totalPages);
            paginationInfo.put("pageSize", size);
            paginationInfo.put("currentPage", page);

            // Response Data
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(currentPageData));
            responseData.set("paginationData", paginationInfo);

            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(
                    responseData, true, "HR-SETTINGS-LIST-SUCCESS", "SUCCESS");
            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            log.error("Invalid input for HRMS settings list", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "HR-SETTINGS-LIST-FAILURE", "WARNING",
                    Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching HRMS settings", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "HR-SETTINGS-LIST-FAILURE", "ERROR",
                    Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PatchMapping("{id}")
    public ResponseEntity<ApiResponse<HRSettingDto>> updateHRSetting(
            @PathVariable Integer id,
            @RequestBody HRSettingDto dto
    ) {
        try {
            HRSettingDto updatedHRSettingDto = branchService.updateHRSetting(id, dto);
            ApiResponse<HRSettingDto> response = new ApiResponse<>(
                    updatedHRSettingDto,
                    true,
                    "HR-SETTINGS-UPDATE-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<HRSettingDto> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HR-SETTINGS-UPDATE-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse); // 409 Conflict for invalid argument
        } catch (ResourceNotFoundException ex) {
            ApiResponse<HRSettingDto> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HR-SETTINGS-NOT-FOUND",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse); // 404 Not Found
        } catch (Exception ex) {
            ApiResponse<HRSettingDto> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HR-SETTINGS-UPDATE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse); // 500 Internal Server Error
        }
    }

    @GetMapping("/branch-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranchList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword
    ) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.Direction.fromString(sortDir), sortBy);

            Map<String, Object> branchDTOList = branchService.getPaginatedBranchList(searchField, searchKeyword, pageable);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    branchDTOList, true, "BRANCH-LIST-SUCCESS", "Success");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "BRANCH-LIST-FAILURE", "Warning", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.ok(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "BRANCH-LIST-FAILURE", "Error", Collections.singletonList(ex.getMessage()));
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PatchMapping("update-branch/{id}")
    public ResponseEntity<ApiResponse<BranchDto>> updateBranch(
            @PathVariable Integer id,
            @RequestBody BranchDto dto
    ) {
        try {
            BranchDto updatedHRSettingDto = branchService.updateBranch(id, dto);
            ApiResponse<BranchDto> response = new ApiResponse<>(
                    updatedHRSettingDto,
                    true,
                    "HR-SETTINGS-UPDATE-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<BranchDto> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HR-SETTINGS-UPDATE-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse); // 409 Conflict for invalid argument
        } catch (ResourceNotFoundException ex) {
            ApiResponse<BranchDto> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HR-SETTINGS-NOT-FOUND",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse); // 404 Not Found
        } catch (Exception ex) {
            ApiResponse<BranchDto> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "HR-SETTINGS-UPDATE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse); // 500 Internal Server Error
        }
    }

    @PostMapping("/save-employee-advance")
    public ResponseEntity<ApiResponse<EmployeeAdvance>> saveOrUpdateEmployeeAdvance(
            @RequestBody @Valid EmployeeAdvanceDto employeeAdvanceDto) {
        try {
            EmployeeAdvance employeeAdvance = branchService.saveOrUpdateEmployeeAdvance(employeeAdvanceDto);
            String message = (employeeAdvanceDto.getId() != null)
                    ? "EMPLOYEE-ADVANCE-UPDATED-SUCCESS"
                    : "EMPLOYEE-ADVANCE-CREATED-SUCCESS";
            ApiResponse<EmployeeAdvance> response = new ApiResponse<>(
                    employeeAdvance,
                    true,
                    message,
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            ApiResponse<EmployeeAdvance> validationErrorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-ADVANCE-VALIDATION-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(validationErrorResponse);
        } catch (Exception e) {
            ApiResponse<EmployeeAdvance> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-ADVANCE-CREATION-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/save-leave-type")
    public ResponseEntity<ApiResponse<LeaveTypes>> saveOrUpdateLeaveType(
            @RequestBody @Valid LeaveTypeDto leaveTypeDto) {
        try {
            Optional<LeaveTypes> existingLeave = leaveTypeRepository.findByLeaveCode(leaveTypeDto.getLeaveCode());

            if (existingLeave.isPresent() && (leaveTypeDto.getId() == null || !existingLeave.get().getId().equals(leaveTypeDto.getId()))) {
                ApiResponse<LeaveTypes> duplicateErrorResponse = new ApiResponse<>(
                        null,
                        false,
                        "LEAVE-TYPE-ALREADY-EXISTS",
                        "Error",
                        Collections.singletonList("A leave type with the same code already exists.")
                );
                return ResponseEntity.status(HttpStatus.OK).body(duplicateErrorResponse);
            }
            LeaveTypes leaveType = branchService.saveOrUpdateLeaveType(leaveTypeDto);
            String message = (leaveTypeDto.getId() != null)
                    ? "LEAVE-TYPE-UPDATED-SUCCESS"
                    : "LEAVE-TYPE-CREATED-SUCCESS";

            ApiResponse<LeaveTypes> response = new ApiResponse<>(
                    leaveType,
                    true,
                    message,
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            ApiResponse<LeaveTypes> validationErrorResponse = new ApiResponse<>(
                    null,
                    false,
                    "LEAVE-TYPE-VALIDATION-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);
        } catch (Exception e) {
            ApiResponse<LeaveTypes> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "LEAVE-TYPE-CREATION-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/hr-settings-by-id/{id}")
    public ResponseEntity<ApiResponse<HRSettingDto>> getHRSettingById(@PathVariable("id") Integer id) {
        try {
            Optional<HRSettingDto> hrSettingDto = branchService.getHRSettingById(id);
            return hrSettingDto
                    .map(dto -> ResponseEntity.ok(new ApiResponse<>(
                            dto,
                            true,
                            "HR-SETTINGS-BY-ID",
                            "SUCCESS",
                            null
                    )))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse<>(
                                    null,
                                    false,
                                    "HR-SETTINGS-BY-ID",
                                    "HR-SETTINGS-NOT-FOUND",
                                    Collections.singletonList("HR Setting not found")
                            )));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            null,
                            false,
                            "HR-SETTINGS-BY-ID",
                            "ERROR",
                            Collections.singletonList(e.getMessage()) // Include exception message
                    ));
        }
    }

    @GetMapping("leave-type-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeaveTypeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> leaveTypeDTOList = branchService.getPaginatedLeaveTypeList(searchKeyword, searchField, pageable);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    leaveTypeDTOList,
                    true,
                    "LEAVE-TYPE-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "LEAVE-TYPE-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "LEAVE-TYPE-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("employee-advance-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeAdvanceList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
        try {
            if (sortBy.equalsIgnoreCase("employeeName")) sortBy = "employeeId";
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> employeeAdvanceListDTOList = branchService.getPaginatedEmployeeAdvanceList(searchKeyword, searchField, pageable);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    employeeAdvanceListDTOList,
                    true,
                    "EMPLOYEE-ADVANCE-LIST-SUCCESS",
                    "Success"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-ADVANCE-LIST-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "EMPLOYEE-ADVANCE-LIST-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

/*    @PostMapping("/createOrUpdate")
    public ResponseEntity<ApiResponse<Branch>> createOrUpdateBranch(@Valid @RequestBody BranchWithAddressDto branchDto) {
        try {
            Branch savedBranch = branchService.createOrUpdateBranch(branchDto);

            ApiResponse<Branch> response = new ApiResponse<>(
                    savedBranch,
                    true,
                    (branchDto.getId() != null) ? "BRANCH-UPDATED-SUCCESS" : "BRANCH-CREATED-SUCCESS",
                    "Information"
            );

            return ResponseEntity.status((branchDto.getId() != null) ? HttpStatus.OK : HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            ApiResponse<Branch> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "BRANCH-SAVE-FAILURE",
                    "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }*/

    //    @PostMapping("/createOrUpdate")
    @PostMapping()
    public ResponseEntity<ApiResponse<Branch>> createOrUpdateBranch(@Valid @RequestBody BranchWithAddressDto branchDto) {
        try {
            if (branchDto.getId() == null) {
                Optional<Branch> existingBranch = branchRepository.findByCode(branchDto.getCode());
                if (existingBranch.isPresent()) {
                    ApiResponse<Branch> existingBranchResponse = new ApiResponse<>(
                            existingBranch.get(),
                            false,
                            "BRANCH-ALREADY-EXISTS",
                            "Warning",
                            Collections.singletonList(
                                    "Branch with code " + branchDto.getCode() + " already exists.")
                    );
                    return ResponseEntity.status(HttpStatus.OK).body(existingBranchResponse); // 409 Conflict
                }
            }
            Branch savedBranch = branchService.createOrUpdateBranch(branchDto);

            ApiResponse<Branch> response = new ApiResponse<>(
                    savedBranch,
                    true,
                    (branchDto.getId() != null) ? "BRANCH-UPDATED-SUCCESS" : "BRANCH-CREATED-SUCCESS",
                    "Information"
            );

            return ResponseEntity.status((branchDto.getId() != null) ? HttpStatus.OK : HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            ApiResponse<Branch> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "BRANCH-SAVE-FAILURE",
                    "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/findBranchIdAndName")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBranchNameAndId() {
        try {
            List<Map<String, Object>> employees = this.branchService.findBranchNameAndId();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "BRANCH_DETAILS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No branch details found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "BRANCH_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "BRANCH_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("nonAssociateHrSettingsEmployees")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> nonAssociateHrSettingsEmployees() {
        try {
            List<Map<String, Object>> nonAssociateEmployees = branchService.nonAssociateEmployees();
            return ResponseEntity.ok(new ApiResponse<>(nonAssociateEmployees, true, "EMPLOYEES_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception ex) {
            return ResponseEntity.ok(new ApiResponse<>(null, true, "EMPLOYEES_LIST_FETCHED_ERROR", "SUCCESS", Collections.singletonList(ex.getMessage())));
        }
    }

    @GetMapping("/leaveTypeDropdownList")
    public ResponseEntity<ApiResponse<Object>> leaveTypeDropdownList() {
        try {
            List<Map<String, Object>> leaveTypeDropdownList = branchService.leaveTypeDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(leaveTypeDropdownList, true, "LEAVE_TYPE_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "LEAVE_TYPE_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}
