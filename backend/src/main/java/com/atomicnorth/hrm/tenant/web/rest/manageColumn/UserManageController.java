package com.atomicnorth.hrm.tenant.web.rest.manageColumn;


import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.exception.BadApiRequestException;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.SesM00UserManageColumnsDTO;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.SesM00UserManageColumnsResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.get_data_dto.SesM00UserManageColumnsGetResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.get_data_dto.UserManageColumnDetailsGetDTO;
import com.atomicnorth.hrm.tenant.service.manageColumn.UserManageService;
import com.atomicnorth.hrm.util.OldApiResponseMessage;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/usermanage")
public class UserManageController {

    private final Logger log = LoggerFactory.getLogger(UserManageController.class);
    @Autowired
    private UserManageService userManageService;

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<Object> saveOrUpdateUserManageColumns(
            @RequestParam Integer userId,
            @RequestParam String pageKey,
            @Valid @RequestBody SesM00UserManageColumnsDTO userManageColumnsDTO) {
        try {
            // Check if userId and pageKey from the request parameters match the DTO
            if (!userManageColumnsDTO.getUserId().equals(userId)) {
                throw new BadApiRequestException("UserId in request parameters and DTO do not match.");
            }

            if (!userManageColumnsDTO.getPageKey().equals(pageKey)) {
                throw new BadApiRequestException("PageKey in request parameters and DTO do not match.");
            }

            // Check if userId and pageKey exist in the database
            if (userManageService.isExisting(userId, pageKey)) {
                // If record exists, call the update method
                SesM00UserManageColumnsResponseDTO updatedResponse = userManageService.updateUserManageColumns(userId, pageKey, userManageColumnsDTO);
                return new ResponseEntity<>(updatedResponse, HttpStatus.OK);
            } else {
                // If record does not exist, call the create method
                SesM00UserManageColumnsResponseDTO createdResponse = userManageService.createUserManageColumns(userManageColumnsDTO);
                return new ResponseEntity<>(createdResponse, HttpStatus.CREATED);
            }
        } catch (BadApiRequestException ex) {
            // Handle custom exceptions
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("Operation failed. " + ex.getMessage())
                    .status(HttpStatus.CONFLICT)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.CONFLICT);
        } catch (Exception ex) {
            // Handle general exceptions
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("An error occurred while processing the request. " + ex.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getDetails")
    public ResponseEntity<Object> getUserManageColumnsByUserIdAndPageKey(
            @RequestParam Integer userId,
            @RequestParam String pageKey) {
        try {
            // Fetch user manage columns based on userId and pageKey
            SesM00UserManageColumnsGetResponseDTO responseDTO = userManageService.getUserManageColumnsByUserIdAndPageKey(userId, pageKey);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            // Handle case where no data is found
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("User manage columns not found for userId: " + userId + " and pageKey: " + pageKey)
                    .status(HttpStatus.NOT_FOUND)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            // Handle general exceptions
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("An error occurred while processing the request: " + ex.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/userManageColumnDetails")
    public ResponseEntity<Object> getUserManageColumnDetailsByUserManageColumnId(
            @RequestParam Integer userManageColumnId) {
        try {
            // Fetch user manage column details based on userManageColumnId
            List<UserManageColumnDetailsGetDTO> detailsDTOs = userManageService.getUserManageColumnDetailsByUserManageColumnId(userManageColumnId);

            // Check if the details were found
            if (detailsDTOs.isEmpty()) {
                OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                        .message("User manage column details not found for userManageColumnId: " + userManageColumnId)
                        .status(HttpStatus.OK)
                        .success(false)
                        .build();
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            }

            return new ResponseEntity<>(detailsDTOs, HttpStatus.OK);
        } catch (Exception ex) {
            // Handle general exceptions
            OldApiResponseMessage responseMessage = OldApiResponseMessage.builder()
                    .message("An error occurred while processing the request: " + ex.getMessage())
                    .status(HttpStatus.OK)
                    .success(false)
                    .build();
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }
    }

    @GetMapping("/getManageColumnDetails")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getManageColumnDetails(
            @RequestParam int userId,
            @RequestParam String pageKey) {
        try {
            String schemaName = TenantContextHolder.getTenant();
            List<Map<String, Object>> columnDetails = userManageService.getManageColumnDetails(userId, pageKey, schemaName);

            if (columnDetails.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse<>(null, false, "COLUMN_DETAILS_NOT_FOUND", "FAILURE",
                                Collections.singletonList("No data found for the provided User ID and Page Key.")));
            }

            return ResponseEntity.ok(new ApiResponse<>(columnDetails, true, "COLUMN_DETAILS_FETCHED", "SUCCESS"));
        } catch (Exception e) {
            log.error("Error occurred while fetching Manage Column Details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(null, false, "COLUMN_DETAILS_FETCH_ERROR", "FAILURE",
                            Collections.singletonList("An unexpected error occurred while fetching column details.")));
        }
    }
}


