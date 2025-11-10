package com.atomicnorth.hrm.tenant.web.rest.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowDelegation;
import com.atomicnorth.hrm.tenant.domain.approvalflow.Level;
import com.atomicnorth.hrm.tenant.repository.approvalflow.ApprovalFlowRepository;
import com.atomicnorth.hrm.tenant.service.approvalflow.ApprovalFlowService;
import com.atomicnorth.hrm.tenant.service.dto.EmployeeDTO;
import com.atomicnorth.hrm.tenant.service.dto.approvalflow.ApprovalFlowDelegationDto;
import com.atomicnorth.hrm.tenant.service.dto.approvalflow.ApprovalFlowDto;
import com.atomicnorth.hrm.tenant.service.dto.approvalflow.LevelMasterDTO;
import com.atomicnorth.hrm.tenant.service.dto.approvalflow.WorkflowRequestDetailsDto;
import com.atomicnorth.hrm.tenant.service.dto.designation.DesignationDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/approvalflow")
public class ApprovalFlowController {

    private final ApprovalFlowService approvalFlowService;

    private final ApprovalFlowRepository approvalFlowRepository;
    public ApprovalFlowController(ApprovalFlowService approvalFlowService,ApprovalFlowRepository approvalFlowRepository){
        this.approvalFlowService = approvalFlowService;
        this.approvalFlowRepository = approvalFlowRepository;
    }
    @GetMapping("/getApprovalFlow")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApprovalFlowRequest(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(defaultValue = "approvalFlowId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> responseData =
                    approvalFlowService.fetchApprovalFlow(pageable, searchColumn, searchValue);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    responseData, true, "APPROVAL-FLOW-RETRIEVED-SUCCESSFULLY", "Information");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null, false, "APPROVAL-FLOW-RETRIEVAL-FAILED", "Error", List.of(ex.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @GetMapping("{approvalId}")
    public ResponseEntity<ApiResponse<ApprovalFlowDto>> getApprovalFlowListBasedOnApprovalId(@PathVariable("approvalId") Integer approvalId){
        ApprovalFlowDto approvalFlowDto = approvalFlowService.findApprovalFlow(approvalId);
        if(approvalFlowDto == null){
            ApiResponse<ApprovalFlowDto> response = new ApiResponse<>(approvalFlowDto , true, "APPROVAL-FLOW-NOT-AVAILABLE", "Information");
            return ResponseEntity.ok(response);
        }
        ApiResponse<ApprovalFlowDto> response = new ApiResponse<>(approvalFlowDto , true, "APPROVAL-FLOW-RETRIEVED-SUCCESSFULLY", "Information");
        return ResponseEntity.ok(response);
    }
    @PostMapping("/approval-flow")
    public ResponseEntity<ApiResponse<ApprovalFlowDto>> saveOrUpdateApprovalFlow(
            @RequestBody ApprovalFlowDto approvalFlowDto) {
        boolean exists;
        if (approvalFlowDto.getApprovalFlowId() == null) {
            exists = approvalFlowRepository.existsByApprovalFlowCodeIgnoreCase(
                    approvalFlowDto.getApprovalFlowCode()
            );
        } else {
            exists = approvalFlowRepository.existsByApprovalFlowCodeIgnoreCaseAndApprovalFlowIdNot(
                    approvalFlowDto.getApprovalFlowCode(),
                    approvalFlowDto.getApprovalFlowId()
            );
        }

        if (exists) {
            ApiResponse<ApprovalFlowDto> response =
                    new ApiResponse<>(null, false, "Approval Flow Code already exists", "ERROR");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        String validationMsg = approvalFlowService.validateDuplicateRequestMappings(approvalFlowDto);
        if (validationMsg != null) {
            ApiResponse<ApprovalFlowDto> response =
                    new ApiResponse<>(null, false, validationMsg, "ERROR");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        ApprovalFlowDto savedDto = approvalFlowService.saveOrUpdateApprovalFlow(approvalFlowDto);
        String message = (approvalFlowDto.getApprovalFlowId() == null)
                ? "APPROVAL-FLOW-SAVED"
                : "APPROVAL-FLOW-UPDATED";
        ApiResponse<ApprovalFlowDto> response =
                new ApiResponse<>(savedDto, true, message, "SUCCESS");
        return ResponseEntity.ok(response);
    }



    @GetMapping("level-list")
    public ResponseEntity<ApiResponse<List<Level>>> getLevelName() {
        List<Level> levelList = approvalFlowService.findLevelName();
        if (levelList == null || levelList.isEmpty()) {
            ApiResponse<List<Level>> response =
                    new ApiResponse<>(null, false, "APPROVAL-FLOW-LEVEL-NOT-AVAILABLE", "Information");
            return ResponseEntity.ok(response);
        }
        ApiResponse<List<Level>> response =
                new ApiResponse<>(levelList, true, "APPROVAL-FLOW-LEVEL-FETCHED", "SUCCESS");
        return ResponseEntity.ok(response);
    }

    @GetMapping("level-list-mapping")
    public ResponseEntity<ApiResponse<List<DesignationDTO>>> getDesignationBasedOnLevelMapping(
            @RequestParam("levelId") Integer levelId) {
        if (levelId == null) {
            ApiResponse<List<DesignationDTO>> response =
                    new ApiResponse<>(null, false, "APPROVAL-FLOW-LEVEL-MAPPING-NOT-AVAILABLE", "FAILURE");
            return ResponseEntity.ok(response);
        }
        List<DesignationDTO> designationList = approvalFlowService.findDesignationBasedOnLevelMapping(levelId);
        if (designationList == null || designationList.isEmpty()) {
            ApiResponse<List<DesignationDTO>> response =
                    new ApiResponse<>(Collections.emptyList(), false, "DESIGNATIONS-NOT-FOUND", "INFO");
            return ResponseEntity.ok(response);
        }
        ApiResponse<List<DesignationDTO>> response =
                new ApiResponse<>(designationList, true, "DESIGNATIONS-FETCHED-SUCCESSFULLY", "SUCCESS");
        return ResponseEntity.ok(response);
    }

    @GetMapping("employee-list-designation")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesBasedOnDesignations(
            @RequestParam("designationsId") Integer[] designationsId) {
        if (designationsId == null || designationsId.length == 0) {
            ApiResponse<List<EmployeeDTO>> response =
                    new ApiResponse<>(null, false, "DESIGNATION-ID-REQUIRED", "FAILURE");
            return ResponseEntity.badRequest().body(response);
        }
        List<EmployeeDTO> employees = approvalFlowService.findEmployeesBasedOnDesignations(designationsId);
        if (employees == null || employees.isEmpty()) {
            ApiResponse<List<EmployeeDTO>> response =
                    new ApiResponse<>(Collections.emptyList(), false, "NO-EMPLOYEES-FOUND", "INFO");
            return ResponseEntity.ok(response);
        }
        ApiResponse<List<EmployeeDTO>> response =
                new ApiResponse<>(employees, true, "EMPLOYEES-FETCHED-SUCCESSFULLY", "SUCCESS");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/delegation/{approvalId}")
    public ResponseEntity<ApiResponse<List<ApprovalFlowDelegation>>> getApprovalFlowDelegationListBasedOnApprovalId(
            @PathVariable("approvalId") Integer approvalId) {

        List<ApprovalFlowDelegation> approvalFlowDelegationList =
                approvalFlowService.findDelegationBasedOnApprovalFlow(approvalId);
        if (approvalFlowDelegationList == null || approvalFlowDelegationList.isEmpty()) {
            ApiResponse<List<ApprovalFlowDelegation>> response =
                    new ApiResponse<>(null, false, "APPROVAL-FLOW-NOT-AVAILABLE", "Error");
            return ResponseEntity.ok(response);
        }
        ApiResponse<List<ApprovalFlowDelegation>> response =
                new ApiResponse<>(approvalFlowDelegationList, true, "APPROVAL-FLOW-RETRIEVED-SUCCESSFULLY", "Information");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/saveOrUpdateApprovalFlowDelegation")
    public ResponseEntity<ApiResponse<List<ApprovalFlowDelegationDto>>> saveAndUpdateApprovalFlowDelegation(
            @RequestBody List<ApprovalFlowDelegationDto> approvalFlowDelegationDtos) {
        List<ApprovalFlowDelegationDto> savedDtos = approvalFlowService.saveAndWorkFlowDelegation(approvalFlowDelegationDtos);

        if (savedDtos == null || savedDtos.isEmpty()) {
            ApiResponse<List<ApprovalFlowDelegationDto>> response = new ApiResponse<>(
                    null,
                    false,
                    "APPROVAL-FLOW-DELEGATION-NOT-AVAILABLE",
                    "Error"
            );
            return ResponseEntity.ok(response);
        }

        String message = (savedDtos.stream().allMatch(d -> d.getId() == null))
                ? "APPROVAL-FLOW-DELEGATION-SAVED-SUCCESSFULLY"
                : "APPROVAL-FLOW-DELEGATION-UPDATED-SUCCESSFULLY";

        ApiResponse<List<ApprovalFlowDelegationDto>> response = new ApiResponse<>(
                savedDtos,
                true,
                message,
                "Information"
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/track/{requestNumber}")
    public ResponseEntity<ApiResponse<WorkflowRequestDetailsDto>> getWorkflowDetailsByRequestNumber(
            @PathVariable("requestNumber") String requestNumber) {
        WorkflowRequestDetailsDto dto = approvalFlowService.getWorkflowDetailsByRequestNumber(requestNumber);
        if (dto == null) {
            return ResponseEntity.ok(
                    new ApiResponse<>(null, false, "WORKFLOW-REQUEST-NOT-FOUND", "Information")
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>(dto, true, "WORKFLOW-REQUEST-RETRIEVED-SUCCESSFULLY", "Information")
        );
    }

    @GetMapping("level-master-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> levelMasterList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "levelName", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> levelMasterList = approvalFlowService.getLevelMasterList(pageable, searchField, searchKeyword);
            return ResponseEntity.ok(new ApiResponse<>(levelMasterList, true, "LEVEL_MASTER_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "LEVEL_MASTER_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("save-level-master")
    public ResponseEntity<ApiResponse<Level>> saveLevelMaster(@RequestBody LevelMasterDTO dto) {
        try {
            Level level = approvalFlowService.saveLevelMaster(dto);
            return ResponseEntity.ok(new ApiResponse<>(level, true, "LEVEL_MASTER_SAVE_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "LEVEL_MASTER_SAVE_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}