package com.atomicnorth.hrm.tenant.web.rest.lookup;

import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.tenant.domain.lookup.LookupCode;
import com.atomicnorth.hrm.tenant.domain.lookup.LookupType;
import com.atomicnorth.hrm.tenant.repository.translation.LookupTypeProjection;
import com.atomicnorth.hrm.tenant.service.dto.lookup.LookupCodeTranslationDTO;
import com.atomicnorth.hrm.tenant.service.dto.lookup.LookupTypeTranslationDTO;
import com.atomicnorth.hrm.tenant.service.lookup.LookupTypeConfigurationService;
import com.atomicnorth.hrm.util.OldApiResponseMessage;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/configuration")
public class LookupTypeConfiguration {

    private final Logger log = LoggerFactory.getLogger(LookupTypeConfiguration.class);

    @Autowired
    private LookupTypeConfigurationService lookupTypeConfigurationService;

    @GetMapping
    public List<LookupTypeProjection> get() {
        return lookupTypeConfigurationService.getAllLookupType();
    }

    @PutMapping("{id}")
    public ResponseEntity<OldApiResponseMessage> updateLookupType(@PathVariable String id, @RequestBody LookupTypeTranslationDTO lookupTypeTranslationDTO) {
        try {
            LookupType lookupType = lookupTypeConfigurationService.updateLookupType(id, lookupTypeTranslationDTO);
            if (lookupType != null) {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                        .message("Lookup updated successfully.")
                        .status(HttpStatus.OK)
                        .success(true)
                        .build();
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            } else {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                        .message("Lookup type " + id + " not found.")
                        .status(HttpStatus.NOT_FOUND)
                        .success(false)
                        .build();
                return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
            }
        } catch (EntityNotFoundException e) {
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("Lookup type not found.")
                    .status(HttpStatus.NOT_FOUND)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("An error occurred while updating the Lookup type.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("{id}")
    public LookupType getLookupTypeById(@PathVariable Integer id) {
        return lookupTypeConfigurationService.getById(id);
    }

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLookupCodeById(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "module", required = false) Long module,
            @RequestParam(value = "function", required = false) Long function,
            @RequestParam(value = "sortBy", defaultValue = "lookupId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") String sortDir,
            @RequestParam(value = "searchColumn", required = false) String searchColumn,
            @RequestParam(value = "searchValue", required = false) String searchValue,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            Map<String, Object> lookupTypes = lookupTypeConfigurationService.getLookupData(id, module, function, searchColumn, searchValue, pageable);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    lookupTypes, true, "LOOKUP-LIST-SUCCESS", "Success"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error occurred while fetching lookupTypes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "ATMCMN-LOOKUPTYPE_FETCH_ERROR", "FAILURE",
                            Collections.singletonList(e.getMessage())));
        }
    }

    @PutMapping("/lookupCode/{id}")
    public ResponseEntity<OldApiResponseMessage> updateLookupCode(@PathVariable Integer id, @RequestBody LookupCodeTranslationDTO lookupCodeTranslationDTO) {
        try {
            LookupCode lookupType = lookupTypeConfigurationService.updateLookupCode(id, lookupCodeTranslationDTO);
            if (lookupType != null) {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                        .message("Lookup code updated successfully.")
                        .status(HttpStatus.OK)
                        .success(true)
                        .build();
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            } else {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                        .message("Lookup code not found.")
                        .status(HttpStatus.NOT_FOUND)
                        .success(false)
                        .build();
                return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
            }
        } catch (EntityNotFoundException e) {
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("Lookup code not found.")
                    .status(HttpStatus.NOT_FOUND)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("An error occurred while updating the Lookup code.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("lookupCode/{id}/{flag}")
    public ResponseEntity<OldApiResponseMessage> deleteLookupCodeById(@PathVariable Integer id, @PathVariable String flag) {
        try {
            LookupCode lookupCode = lookupTypeConfigurationService.deleteLookupCodeById(id, flag);
            if (lookupCode != null) {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                        .message("Lookup code deleted successfully.")
                        .status(HttpStatus.OK)
                        .success(true)
                        .build();
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            } else {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                        .message("Lookup code not found.")
                        .status(HttpStatus.NOT_FOUND)
                        .success(false)
                        .build();
                return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
            }
        } catch (EntityNotFoundException e) {
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("Lookup code not found.")
                    .status(HttpStatus.NOT_FOUND)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("An error occurred while deleting the Lookup code.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*LookUp Creation Code And type with translation*/
    @PostMapping(value = "lookupTypeTranslation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<LookupTypeTranslationDTO>> addLookupTypeTranslation(@RequestBody LookupTypeTranslationDTO lookupTypeDTO) {
        try {
            LookupTypeTranslationDTO lookupType = lookupTypeConfigurationService.addLookTypeTranslation(lookupTypeDTO);
            ApiResponse<LookupTypeTranslationDTO> response = new ApiResponse<>(
                    lookupType,
                    true,
                    "LOOKUP-CREATED-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<LookupTypeTranslationDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "LOOKUP-CREATED-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "lookupCodeTranslation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<LookupCodeTranslationDTO>> addLookupCodeTranslation(@RequestBody LookupCodeTranslationDTO lookupCodeTranslationDTO) {
        try {
            LookupCodeTranslationDTO lookupCode = lookupTypeConfigurationService.addLookCodeTranslation(lookupCodeTranslationDTO);
            ApiResponse<LookupCodeTranslationDTO> response = new ApiResponse<>(
                    lookupCode,
                    true,
                    "LOOKUP-CODE-CREATED-SUCCESS",
                    "Information"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<LookupCodeTranslationDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "LOOKUP-CODE-CREATED-FAILURE",
                    "Error",
                    Collections.singletonList(e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/lookupTypeCodes")
    public List<Map<String, Object>> getLookupTypeCodes(
            @RequestParam(required = false) int p_app_module,
            @RequestParam(required = false) int p_module_function,
            @RequestParam String p_lookup_type) {
        return lookupTypeConfigurationService.callGetLookUpTypeCodes(p_lookup_type);
    }

    @GetMapping("/lookupCode")
    public List<LookupCode> getAllLookupCodesById(@RequestParam Integer lookupId) {
        return lookupTypeConfigurationService.getAllLookupCodesById(lookupId);
    }
}
