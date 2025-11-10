package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.domain.InterviewType;
import com.atomicnorth.hrm.tenant.repository.InterviewTypeRepository;
import com.atomicnorth.hrm.tenant.service.InterviewTypeService;
import com.atomicnorth.hrm.tenant.service.dto.InterviewTypeDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
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

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interview_type")
@EnableTransactionManagement
public class InterviewTypeController {

    private final Logger log = LoggerFactory.getLogger(InterviewTypeController.class);

    @Autowired
    private InterviewTypeService interviewTypeService;

    @Autowired
    private InterviewTypeRepository interviewTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseEntity<ApiResponse<?>> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage()
                ));
        return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_VALIDATION_ERROR", "FAILURE",
                new ArrayList<>(errors.values())), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> saveInterviewType(
            @Valid @RequestBody InterviewTypeDTO interviewTypeDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        Long id = interviewTypeDTO.getInterviewTypeId();
        try {
            // Update operation if ID is provided
            if (id != null) {
                Optional<InterviewType> existingInterviewType = interviewTypeRepository.findByNameIgnoreCase(interviewTypeDTO.getName());
                if (existingInterviewType.isPresent() && !existingInterviewType.get().getInterviewTypeId().equals(id)) {
                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_TYPE_DUPLICATE", "FAILURE",
                                    Collections.singletonList("A Interview type with name '" + interviewTypeDTO.getName()
                                            + "' already exists for the selected company.")),
                            HttpStatus.OK);
                }
                InterviewTypeDTO updatedInterviewTypeDTO = interviewTypeService.updateInterviewType(id, interviewTypeDTO);
                return ResponseEntity.ok(new ApiResponse<>(updatedInterviewTypeDTO, true, "ATMCMN_INTERVIEW_TYPE_UPDATED", "SUCCESS"));
            } else {
                if (interviewTypeRepository.findByNameIgnoreCase(interviewTypeDTO.getName()).isPresent()) {
                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_TYPE_DUPLICATE", "FAILURE",
                                    Collections.singletonList("A Interview type with name '" + interviewTypeDTO.getName()
                                            + "' already exists for the selected company.")),
                            HttpStatus.OK);
                }
                InterviewTypeDTO savedInterviewTypeDTO = interviewTypeService.createInterviewTpye(interviewTypeDTO);
                return new ResponseEntity<>(new ApiResponse<>(savedInterviewTypeDTO, true, "ATMCMN_INTERVIEW_TYPE_CREATED", "SUCCESS"), HttpStatus.CREATED);
            }
        } catch (Exception e) {
            log.error("Error occurred while saving Interview type", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_TYPE_SAVE_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllInterviews(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "interviewTypeId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {

        try {
            List<InterviewTypeDTO> interviewTypeDTOS = interviewTypeService.getAllInterviewType();
            // Convert DTO List to List of Maps
            List<Map<String, Object>> fullData = interviewTypeDTOS.stream()
                    .map(dto -> objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {
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
                    // Handle numeric comparisons
                    if (obj1 instanceof Number && obj2 instanceof Number) {
                        return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
                    }
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                };
                // Apply descending order if required
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
            // Response Data
            ObjectNode responseData = objectMapper.createObjectNode();
            responseData.set("result", objectMapper.valueToTree(currentPageData));
            responseData.put("totalElements", totalItems);
            responseData.put("totalPages", totalPages);
            responseData.put("pageSize", size);
            responseData.put("currentPage", page);
            ApiResponse<ObjectNode> apiResponse = new ApiResponse<>(
                    responseData, true, "ATMCMN_INTERVIEW_TYPE_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching Interview type", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "ATMCMN_INTERVIEW_TYPE_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewTypeDTO>> getInterviewTypeById(@PathVariable Long id) {
        try {
            return interviewTypeService.getInterviewTypeById(id)
                    .map(interviewType -> ResponseEntity.ok(new ApiResponse<>(
                            interviewType, true, "ATMCMN_INTERVIEW_TYPE_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_TYPE_NOT_FOUND", "FAILURE",
                                    Collections.singletonList("Interview Type not found with ID: " + id)),
                            HttpStatus.OK));
        } catch (Exception e) {
            log.error("Error occurred while fetching Interview Type by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_TYPE_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findInterviewTypeIdAndName")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInterviewTypeNameAndId() {
        try {
            List<Map<String, Object>> employees = this.interviewTypeService.findInterviewTypeNameAndId();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "INTERVIEW_TYPE_NAME_ID_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No Interview Type found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "INTERVIEW_TYPE_NAME_ID_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Interview type Name and Id", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "INTERVIEW_TYPE_NAME_ID_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("/interviewTypeDropdownList")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> interviewTypeDropdownList() {
        try {
            List<Map<String, Object>> interviewTypeDropdownList = interviewTypeService.interviewTypeDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(interviewTypeDropdownList, true, "INTERVIEW_TYPE_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "INTERVIEW_TYPE_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }



}
