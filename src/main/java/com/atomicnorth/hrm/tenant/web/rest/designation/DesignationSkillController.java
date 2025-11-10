package com.atomicnorth.hrm.tenant.web.rest.designation;

import com.atomicnorth.hrm.tenant.service.designation.DesignationSkillService;
import com.atomicnorth.hrm.tenant.service.dto.designation.DesignationDTO;
import com.atomicnorth.hrm.tenant.service.dto.designation.DesignationSkillResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.designation.SkillSetDTO;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/designation")
public class DesignationSkillController {
    private final Logger log = LoggerFactory.getLogger(DesignationSkillController.class);
    @Autowired
    private DesignationSkillService designationSkillService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<DesignationDTO>> createDesignation(@Valid @RequestBody DesignationDTO designationDTO) {
        try {
            DesignationDTO responseMessage = designationSkillService.createDesignation(designationDTO);

            ApiResponse<DesignationDTO> response = new ApiResponse<>(
                    responseMessage,
                    true,
                    "DESIGNATION-CREATED-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException ex) {
            // Check if this is a duplicate designation error
            if ("DUPLICATE_DESIGNATION".equals(ex.getMessage())) {
                ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                        null,
                        false,
                        "DESIGNATION-DUPLICATE-FAILURE",
                        "Warning",
                        Collections.singletonList("Designation name already exists.")
                );
                return ResponseEntity.ok(errorResponse);

            } else {
                ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                        null,
                        false,
                        "DESIGNATION-CREATED-FAILURE",
                        "Warning",
                        Collections.singletonList(ex.getMessage())
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

        } catch (Exception ex) {
            ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                    designationDTO,
                    false,
                    "DESIGNATION-CREATED-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/update/{designationId}")
    public ResponseEntity<ApiResponse<DesignationDTO>> updateDesignation(
            @PathVariable("designationId") Integer designationId,
            @Validated @RequestBody DesignationDTO designationDTO) {
        try {
            DesignationDTO updatedDesignation = designationSkillService.updateDesignation(designationId, designationDTO);

            // Create success response
            ApiResponse<DesignationDTO> response = new ApiResponse<>(
                    updatedDesignation,
                    true,
                    "DESIGNATION-UPDATE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException ex) {
            if ("DUPLICATE_DESIGNATION".equals(ex.getMessage())) {
                ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                        null,
                        false,
                        "DESIGNATION-DUPLICATE-FAILURE",
                        "Warning",
                        Collections.singletonList("Designation name already exists.")
                );
                return ResponseEntity.ok(errorResponse);
            }
            ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-UPDATE-FAILURE",
                    "Warning",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        catch (EntityNotFoundException e) {
            // Create not found error response
            ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-UPDATE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            // Log error for debugging
            e.printStackTrace();

            // Create internal server error response
            ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-UPDATE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @GetMapping("/{designationId}")
    public ResponseEntity<ApiResponse<DesignationDTO>> getDesignationById(@PathVariable("designationId") Integer designationId) {
        try {
            // Fetch designation details
            DesignationDTO dto = designationSkillService.getDesignationById(designationId);

            // Create success response
            ApiResponse<DesignationDTO> response = new ApiResponse<>(
                    dto,
                    true,
                    "DESIGNATION-DATA-RETRIEVE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException e) {
            // Create not found error response
            ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-DATA-RETRIEVE-FAILURE",
                    "Error"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            // Create internal server error response
            ApiResponse<DesignationDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-DATA-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllDesignations(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {

        log.debug("REST request to get paginated Designations record");
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> allJobOpening = designationSkillService.getPaginatedDesignations(pageable, searchField, searchKeyword);


            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    allJobOpening,
                    true,
                    "JOB-OPENING-DATA-RETRIEVE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error response
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "JOB-OPENING-DATA-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @DeleteMapping("/delete/{designationId}")
    public ResponseEntity<ApiResponse<String>> deleteDesignation(@PathVariable Integer designationId) {
        try {
            // Call service to delete designation
            String responseMessage = designationSkillService.deleteDesignation(designationId);

            // Create success response
            ApiResponse<String> response = new ApiResponse<>(
                    responseMessage,
                    true,
                    "DESIGNATION-DELETE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException ex) {
            // Create not found error response
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-DELETE-FAILURE",
                    "Error"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception ex) {
            // Create internal server error response
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-DELETE-FAILURE",
                    "Error",
                    Collections.singletonList(ex.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/allSkillSet")
    public ResponseEntity<ApiResponse<List<SkillSetDTO>>> getAllSkills() {
        try {
            List<SkillSetDTO> skills = designationSkillService.getAllSkills();
            ApiResponse<List<SkillSetDTO>> response = new ApiResponse<>(
                    skills,
                    true,
                    "SKILL-SET-DATA-RETRIEVE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<SkillSetDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "SKILL-SET-DATA-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getSkillNameByDesignationId")
    public ResponseEntity<ApiResponse<List<DesignationSkillResponseDTO>>> getDesignationSkillsById(
            @RequestParam("designationId") Integer designationId) {
        try {
            List<DesignationSkillResponseDTO> skills = designationSkillService.getDesignationSkillsById(designationId);
            if (skills == null || skills.isEmpty()) {
                ApiResponse<List<DesignationSkillResponseDTO>> errorResponse = new ApiResponse<>(
                        null,
                        false,
                        "DESIGNATION-SKILLS-RETRIEVE-FAILURE",
                        "Error",
                        Collections.singletonList("No skills found for designation ID: " + designationId)
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            ApiResponse<List<DesignationSkillResponseDTO>> response = new ApiResponse<>(
                    skills,
                    true,
                    "DESIGNATION-SKILLS-RETRIEVE-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (EntityNotFoundException e) {
            ApiResponse<List<DesignationSkillResponseDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-SKILLS-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList("Skills not found for designation ID: " + designationId)
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<List<DesignationSkillResponseDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "DESIGNATION-SKILLS-RETRIEVE-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/findDesignationIdAndName")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDesignationNameAndId() {
        try {
            List<Map<String, Object>> employees = designationSkillService.findDesignationNameAndId();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse<>(null, false, "DESIGNATION_DETAILS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No Designation details found.")),
                        HttpStatus.OK);
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(employees, true, "DESIGNATION_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Designation details", e);
            return new ResponseEntity<>(
                    new ApiResponse<>(null, false, "DESIGNATION_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())),
                    HttpStatus.OK);
        }
    }

}