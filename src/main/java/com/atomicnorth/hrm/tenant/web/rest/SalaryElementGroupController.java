package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.domain.SalaryElementGroup;
import com.atomicnorth.hrm.tenant.repository.SalaryElementGroupRepository;
import com.atomicnorth.hrm.tenant.service.SalaryElementGroupService;
import com.atomicnorth.hrm.tenant.service.dto.SalaryElementDTO;
import com.atomicnorth.hrm.tenant.service.dto.SalaryElementGroupDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/salaryElementGroup")
@EnableTransactionManagement
public class SalaryElementGroupController {

    private final Logger log = LoggerFactory.getLogger(SalaryElementGroupController.class);

    @Autowired
    private SalaryElementGroupService salaryElementGroupService;

    @Autowired
    private SalaryElementGroupRepository salaryElementGroupRepository;

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
            @Valid @RequestBody SalaryElementGroupDTO salaryElementGroupDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        Long id = salaryElementGroupDTO.getGroupId();
        try {
            // Validate duplicate Element Type Code for same company in the request
            if (salaryElementGroupDTO.getSalaryElements() != null && !salaryElementGroupDTO.getSalaryElements().isEmpty()) {
                Set<Integer> salaryElementSet = new HashSet<>();
                for (SalaryElementDTO salaryElementDTO : salaryElementGroupDTO.getSalaryElements()) {
                    Integer uniqueKey = salaryElementDTO.getElementType();
                    if (!salaryElementSet.add(uniqueKey)) {
                        return new ResponseEntity<>(
                                new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_TYPE_DUPLICATE_REQUEST", "FAILURE",
                                        Collections.singletonList("Salary Element with Element Type: " + salaryElementDTO.getElementType() + " is duplicated in the request.")),
                                HttpStatus.BAD_REQUEST);
                    }
                }
            }
            if (id == null) {
                // Create operation
                if (salaryElementGroupRepository.findByGroupNameIgnoreCaseAndCompany(salaryElementGroupDTO.getGroupName(), salaryElementGroupDTO.getCompany()).isPresent()) {
                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_DUPLICATE", "FAILURE",
                                    Collections.singletonList("A Salary Element Group with name '" + salaryElementGroupDTO.getGroupName()
                                            + "' already exists for the selected company.")),
                            HttpStatus.CONFLICT);
                }
                SalaryElementGroupDTO savedElementGroupDTO = salaryElementGroupService.createSalaryElementGroup(salaryElementGroupDTO);
                return new ResponseEntity<>(
                        new ApiResponse<>(savedElementGroupDTO, true, "ATMCMN_SALARY_ELEMENT_GROUP_CREATED", "SUCCESS"),
                        HttpStatus.CREATED);
            } else {
                //Update Operation - Check for company + groupName uniqueness (excluding current record)
                Optional<SalaryElementGroup> existingGroup = salaryElementGroupRepository.findByGroupNameIgnoreCaseAndCompany(
                        salaryElementGroupDTO.getGroupName(), salaryElementGroupDTO.getCompany());
                if (existingGroup.isPresent() && !existingGroup.get().getGroupId().equals(id)) {
                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_DUPLICATE", "FAILURE",
                                    Collections.singletonList("A Salary Element Group with name '" + salaryElementGroupDTO.getGroupName()
                                            + "' already exists for the selected company.")),
                            HttpStatus.CONFLICT);
                }
                SalaryElementGroupDTO updatedElementGroupDTO = salaryElementGroupService.updateSalaryElementGroup(id, salaryElementGroupDTO);
                return ResponseEntity.ok(
                        new ApiResponse<>(updatedElementGroupDTO, true, "ATMCMN_SALARY_ELEMENT_GROUP_UPDATED", "SUCCESS"));
            }
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_NOT_FOUND", "FAILURE",
                            Collections.singletonList("SALARY ELEMENT GROUP not found with ID: " + id)),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while saving/updating SALARY ELEMENT GROUP", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllSalaryElementGroup(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "groupId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {

        try {
            List<SalaryElementGroupDTO> salaryElementGroupDTOS = salaryElementGroupService.findAllSalaryElementGroup(
                    searchField, searchKeyword, sortBy, sortDir, page, size
            );
            // Pagination info
            ObjectNode paginationInfo = objectMapper.createObjectNode();
            paginationInfo.put("totalElements", salaryElementGroupDTOS.size());
            paginationInfo.put("pageSize", size);
            paginationInfo.put("currentPage", page);
            paginationInfo.put("totalPages", (int) Math.ceil((double) salaryElementGroupDTOS.size() / size));
            // Build response
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("salaryElementGroup", objectMapper.valueToTree(salaryElementGroupDTOS));
            responseData.set("paginationData", paginationInfo);
            return ResponseEntity.ok(new ApiResponse<>(responseData, true, "ATMCMN_SALARY_ELEMENT_GROUP_FETCHED", "SUCCESS"));
        } catch (RuntimeException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())));
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching Salary Element Group", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_FETCH_ERROR", "FAILURE",
                            Collections.singletonList("An unexpected error occurred while fetching Salary Element Group data")));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalaryElementGroupDTO>> getSalaryElementGroupById(@PathVariable Long id) {
        try {
            return salaryElementGroupService.getSalaryElementGroupById(id)
                    .map(salaryElementGroup -> ResponseEntity.ok(new ApiResponse<>(salaryElementGroup, true, "ATMCMN_SALARY_ELEMENT_GROUP_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_NOT_FOUND", "FAILURE",
                                    Collections.singletonList("Salary Element Group not found with ID: " + id)),
                            HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error occurred while fetching Salary Element Group by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSalaryElementGroup(@PathVariable Long id) {
        try {
            salaryElementGroupService.deleteSalaryElementGroupById(id);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "ATMCMN_SALARY_ELEMENT_GROUP_DELETED", "SUCCESS"));
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_NOT_FOUND", "FAILURE",
                            Collections.singletonList("Salary Element Group not found with ID: " + id)),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while deleting Salary Element Group", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_DELETE_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAllActiveSalaryElementGroup")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllActiveSalaryElementGroups() {
        try {
            List<SalaryElementGroup> activeGroups = salaryElementGroupService.findAllActiveSalaryElementGroup();

            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("salaryElementGroups", objectMapper.valueToTree(activeGroups));

            return ResponseEntity.ok(
                    new ApiResponse<>(responseData, true, "ATMCMN_SALARY_ELEMENT_GROUP_FETCHED", "SUCCESS")
            );

        } catch (RuntimeException e) {
            log.error("Runtime exception while fetching active Salary Element Groups: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage()))
            );

        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching active Salary Element Groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(null, false, "ATMCMN_SALARY_ELEMENT_GROUP_FETCH_ERROR", "FAILURE",
                            Collections.singletonList("An unexpected error occurred while fetching Salary Element Group data"))
            );
        }
    }

    @DeleteMapping("deleteSalaryElementById/{groupId}/{elementId}")
    public ResponseEntity<ApiResponse<Object>> deleteById(@PathVariable Long groupId, @PathVariable Long elementId) {
        try {
            salaryElementGroupService.deleteSalaryElementById(groupId, elementId);
            return ResponseEntity.ok(new ApiResponse<>(null, true, "SALARY_ELEMENT_DELETED_SUCCESSFULLY", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "SALARY_ELEMENT_DELETION_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}
