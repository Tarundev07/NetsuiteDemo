package com.atomicnorth.hrm.tenant.web.rest.keycloak.role;

import com.atomicnorth.hrm.tenant.service.dto.keycloak.role.KeycloakRoleAssignToUserRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.role.KeycloakUserRoleDTO;
import com.atomicnorth.hrm.tenant.service.keycloak.role.KeycloakRoleAssignToUserService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/roles-assigned")
public class KeycloakRoleAssignToUserController {

    private final KeycloakRoleAssignToUserService keycloakRoleAssignToUserService;

    public KeycloakRoleAssignToUserController(KeycloakRoleAssignToUserService keycloakRoleAssignToUserService) {
        this.keycloakRoleAssignToUserService = keycloakRoleAssignToUserService;
    }

    private String extractKeycloakErrorMessage(WebClientResponseException ex) {
        try {
            return ex.getResponseBodyAsString();
        } catch (Exception e) {
            return "Unknown error occurred.";
        }
    }

    @GetMapping("/assign/{realm}/{userId}/{clientId}")
    public ResponseEntity<ApiResponse<List<KeycloakUserRoleDTO>>> getUserRoles(
            @PathVariable String realm,
            @PathVariable String userId,
            @PathVariable String clientId,
            @RequestHeader("keycloakToken") String keycloakToken) {
        try {
            List<KeycloakUserRoleDTO> roles = keycloakRoleAssignToUserService.getUserRoles(realm, userId, clientId, keycloakToken);

            ApiResponse<List<KeycloakUserRoleDTO>> response = new ApiResponse<>(
                    roles,
                    true,
                    "USER-ROLE-RETRIEVE-SUCCESS",
                    "Roles retrieved successfully."
            );
            return ResponseEntity.ok(response);

        } catch (WebClientResponseException ex) {
            log.error("Keycloak API Error: {}", ex.getMessage());
            ApiResponse<List<KeycloakUserRoleDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "USER-ROLE-RETRIEVE-FAILURE",
                    "Error",
                    List.of("Failed to retrieve user roles: " + ex.getMessage())
            );
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);

        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage(), ex);
            ApiResponse<List<KeycloakUserRoleDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "USER-ROLE-RETRIEVE-ERROR",
                    "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<String>> removeRoleFromUser(
            @RequestParam String realm,
            @RequestParam String userId,
            @RequestParam String clientId,
            @RequestHeader("token") String token,
            @RequestBody List<KeycloakRoleAssignToUserRequestDTO> roles) {

        try {
            if (roles == null || roles.isEmpty()) {
                throw new IllegalArgumentException("Role list cannot be empty");
            }

            // Check if the role is assigned before removing
            boolean isAssigned = keycloakRoleAssignToUserService.isRoleAlreadyAssigned(realm, userId, clientId, roles, token);
            if (!isAssigned) {
                ApiResponse<String> response = new ApiResponse<>(
                        null, false, "ROLE-NOT-ASSIGNED", "Error",
                        List.of("The user does not have the assigned role.")
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Remove the assigned role
            keycloakRoleAssignToUserService.removeRoleFromUser(realm, userId, clientId, roles, token);

            ApiResponse<String> response = new ApiResponse<>(
                    "Role removed successfully",
                    true,
                    "ROLE-REMOVE-SUCCESS",
                    "User role mapping updated."
            );
            return ResponseEntity.ok(response);

        } catch (WebClientResponseException ex) {
            String errorMessage = extractKeycloakErrorMessage(ex);
            log.error("Keycloak role removal failed: Response Code: {}, Response Body: {}",
                    ex.getStatusCode(), errorMessage);

            ApiResponse<String> errorResponse = new ApiResponse<>(
                    null, false, "ROLE-REMOVE-FAILURE", "Error",
                    List.of("Failed to remove role: " + errorMessage)
            );
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);

        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage(), ex);
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    null, false, "ROLE-REMOVE-ERROR", "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }

    @PostMapping("/assignRoleToUserTest")
    public ResponseEntity<ApiResponse<String>> assignRoleToUserTest(
            @RequestParam String realm,
            @RequestParam String userId,
            @RequestParam String clientId,
            @RequestHeader("token") String token,
            @RequestBody List<KeycloakRoleAssignToUserRequestDTO> roles) {

        try {
            if (roles == null || roles.isEmpty()) {
                throw new IllegalArgumentException("Role list cannot be empty");
            }

            // Call Service Layer (Only Keycloak operation)
            keycloakRoleAssignToUserService.assignRoleToKeycloak(realm, userId, clientId, roles, token);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Role assigned successfully", true, "ROLE-ASSIGN-SUCCESS", "User role mapping completed in Keycloak."));

        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "ROLE-ASSIGN-ERROR", "Error", List.of(ex.getMessage(), "Please contact support.")));
        }
    }


    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> checkKeycloakHealth(
            @RequestParam String realm,
            @RequestHeader("Authorization") String token) {  //  Get Token from Header

        //  Remove "Bearer " if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean isAvailable = keycloakRoleAssignToUserService.isKeycloakAvailable(realm, token);

        if (isAvailable) {
            return ResponseEntity.ok(new ApiResponse<>("Keycloak is UP", true, "KEYCLOAK-AVAILABLE", "API is accessible"));
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiResponse<>("Keycloak is DOWN", false, "KEYCLOAK-DOWN", "API is not accessible"));
        }
    }

}

