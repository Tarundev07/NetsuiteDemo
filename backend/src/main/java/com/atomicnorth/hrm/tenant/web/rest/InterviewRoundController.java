package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.tenant.domain.InterviewRound;
import com.atomicnorth.hrm.tenant.repository.InterviewRoundRepository;
import com.atomicnorth.hrm.tenant.service.InterviewRoundService;
import com.atomicnorth.hrm.tenant.service.dto.InterviewRoundDTO;
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
@RequestMapping("/api/interview_round")
@EnableTransactionManagement
public class InterviewRoundController {

    private final Logger log = LoggerFactory.getLogger(InterviewRoundController.class);

    @Autowired
    private InterviewRoundService interviewRoundService;

    @Autowired
    private InterviewRoundRepository interviewRoundRepository;

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
            @Valid @RequestBody InterviewRoundDTO interviewRoundDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        Long id = interviewRoundDTO.getInterviewRoundId();
        try {
            // Update operation if ID is provided
            if (id != null) {
                Optional<InterviewRound> existingInterviewRound = interviewRoundRepository.findByInterviewRoundNameIgnoreCase(interviewRoundDTO.getInterviewRoundName());
                if (existingInterviewRound.isPresent() && !existingInterviewRound.get().getInterviewRoundId().equals(id)) {
                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_ROUND_DUPLICATE", "FAILURE",
                                    Collections.singletonList("A Interview Round with name '" + existingInterviewRound.get().getInterviewRoundName()
                                            + "' already exists.")),
                            HttpStatus.OK);
                }
                InterviewRoundDTO updatedInterviewRoundDTO = interviewRoundService.updateInterviewRound(id, interviewRoundDTO);
                return ResponseEntity.ok(new ApiResponse<>(updatedInterviewRoundDTO, true, "ATMCMN_INTERVIEW_ROUND_UPDATED", "SUCCESS"));
            } else {
                // Create operation
                if (interviewRoundRepository.findByInterviewRoundNameIgnoreCase(interviewRoundDTO.getInterviewRoundName()).isPresent()) {
                    return new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_ROUND_DUPLICATE", "FAILURE",
                                    Collections.singletonList("A Interview Round with name '" + interviewRoundDTO.getInterviewRoundName()
                                            + "' already exists.")),
                            HttpStatus.OK);
                }
                InterviewRoundDTO savedInterviewRoundDTO = interviewRoundService.createInterviewRound(interviewRoundDTO);
                return new ResponseEntity<>(new ApiResponse<>(savedInterviewRoundDTO, true, "ATMCMN_INTERVIEW_ROUND_CREATED", "SUCCESS"), HttpStatus.CREATED);
            }
        } catch (Exception e) {
            log.error("Error occurred while saving Interview Round", e);
            return new ResponseEntity<>(new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_ROUND_SAVE_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewRoundDTO>> getInterviewTypeById(@PathVariable Long id) {
        try {
            return interviewRoundService.getInterviewRoundById(id)
                    .map(interviewRound -> ResponseEntity.ok(new ApiResponse<>(
                            interviewRound, true, "ATMCMN_INTERVIEW_ROUND_FETCHED", "SUCCESS")))
                    .orElse(new ResponseEntity<>(
                            new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_ROUND_NOT_FOUND", "FAILURE",
                                    Collections.singletonList("Interview Round not found with ID: " + id)),
                            HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error occurred while fetching Interview Round by ID", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "ATMCMN_INTERVIEW_ROUND_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<ObjectNode>> getAllInterviewRounds(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "interviewRoundId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "searchField", required = false) String searchField) {

        try {
            List<InterviewRoundDTO> interviewRounds = interviewRoundService.findAllInterviewRounds();
            // Convert DTO List to List of Maps
            List<Map<String, Object>> fullData = interviewRounds.stream()
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
                    // Convert to string and compare
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
                    responseData, true, "ATMCMN_INTERVIEW_ROUNDS_FETCHED", "SUCCESS");
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error occurred while fetching Interview Rounds", e);
            ApiResponse<ObjectNode> errorResponse = new ApiResponse<>(
                    null, false, "ATMCMN_INTERVIEW_ROUNDS_FETCH_ERROR", "FAILURE",
                    Collections.singletonList(e.getMessage()));
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findInterviewRoundIdAndName")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInterviewRoundNameAndId() {
        try {
            List<Map<String, Object>> employees = this.interviewRoundService.findInterviewRoundNameAndId();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "INTERVIEW_ROUND_NAME_ID_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No Interview Round found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "INTERVIEW_ROUND_NAME_ID_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Interview Round Name and Id", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "INTERVIEW_ROUND_NAME_ID_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

    @GetMapping("/interviewRoundDropdownList")
    private ResponseEntity<ApiResponse<List<Map<String, Object>>>> interviewRoundDropdownList() {
        try {
            List<Map<String, Object>> interviewRoundDropdownList = interviewRoundService.interviewRoundDropdownList();
            return ResponseEntity.ok(new ApiResponse<>(interviewRoundDropdownList, true, "INTERVIEW_ROUND_LIST_FETCHED_SUCCESS", "SUCCESS"));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "COMPANY_LIST_FETCHED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

}
