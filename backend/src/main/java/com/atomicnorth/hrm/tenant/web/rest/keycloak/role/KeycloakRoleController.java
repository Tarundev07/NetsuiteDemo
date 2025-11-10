package com.atomicnorth.hrm.tenant.web.rest.keycloak.role;


import com.atomicnorth.hrm.tenant.service.dto.keycloak.role.KeycloakRoleRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.role.KeycloakRoleResponseDTO;
import com.atomicnorth.hrm.tenant.service.keycloak.role.KeycloakRoleService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
public class KeycloakRoleController {

    private final KeycloakRoleService keycloakRoleService;

    @PostMapping("/create/{realm}/{clientId}")
    public ResponseEntity<ApiResponse<String>> createRole(
            @PathVariable String realm,
            @PathVariable String clientId,
            @RequestHeader("keycloakToken") String keycloakToken,
            @Valid @RequestBody KeycloakRoleRequestDTO roleRequest) {
        try {
            String responseMessage = keycloakRoleService.createRole(realm, clientId, roleRequest, keycloakToken);

            ApiResponse<String> response = new ApiResponse<>(
                    responseMessage,
                    true,
                    "ROLE-CREATE-SUCCESS",
                    "Role has been created successfully."
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (WebClientResponseException ex) {
            String errorMessage = extractKeycloakErrorMessage(ex);

            ApiResponse<String> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ROLE-CREATE-FAILURE",
                    "Error",
                    List.of("Failed to create role: " + errorMessage)
            );
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);

        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage(), ex);
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ROLE-CREATE-ERROR",
                    "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private String extractKeycloakErrorMessage(WebClientResponseException ex) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("errorMessage")) {
                return jsonNode.get("errorMessage").asText();
            } else if (jsonNode.has("error")) {
                return jsonNode.get("error").asText();
            }
        } catch (Exception e) {
            return "Unknown error occurred.";
        }
        return "Unknown error occurred.";
    }


    @GetMapping("/getAllRoles/{realm}/{clientId}")
    public ResponseEntity<Mono<ApiResponse<List<KeycloakRoleResponseDTO>>>> getAllRoles(
            @PathVariable String realm,
            @PathVariable String clientId,
            @RequestHeader("token") String token,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            String accessToken = token.replace("Bearer ", "");

            Mono<List<KeycloakRoleResponseDTO>> roles = keycloakRoleService.getAllRoles(
                    realm, clientId, accessToken, searchColumn, searchValue, sortBy, sortDir);

            ApiResponse<List<KeycloakRoleResponseDTO>> response = new ApiResponse<>(
                    roles.block(), // Blocking to return ResponseEntity
                    true,
                    "ROLE-FETCH-SUCCESS",
                    "Fetched roles successfully"
            );

            return ResponseEntity.status(HttpStatus.OK).body(Mono.just(response));
        } catch (Exception ex) {
            ApiResponse<List<KeycloakRoleResponseDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "ROLE-FETCH-FAILURE",
                    "Error fetching roles",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Mono.just(errorResponse));
        }
    }



    /*TEST*/

    @PostMapping("/createTest/{realm}")
    public ResponseEntity<ApiResponse<String>> createRole(
            @PathVariable String realm,
            @RequestHeader("keycloakToken") String keycloakToken,
            @Valid @RequestBody KeycloakRoleRequestDTO roleRequest) {
        try {
            String responseMessage = keycloakRoleService.createRealmRoleTest(realm, roleRequest, keycloakToken);

            ApiResponse<String> response = new ApiResponse<>(
                    responseMessage,
                    true,
                    "ROLE-CREATE-SUCCESS",
                    "Role has been created successfully."
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (WebClientResponseException ex) {
            String errorMessage = extractKeycloakErrorMessage(ex);
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    null, false, "ROLE-CREATE-FAILURE", "Error",
                    List.of("Failed to create role: " + errorMessage)
            );
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);

        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage(), ex);
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    null, false, "ROLE-CREATE-ERROR", "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/createRoleInKeycloak/{realm}/{clientId}")
    public ResponseEntity<ApiResponse<String>> createRoleInKeycloak(
            @PathVariable String realm,
            @PathVariable String clientId,
            @RequestHeader("keycloakToken") String token,
            @RequestParam(value = "saveToDatabase", defaultValue = "true") String saveToDatabaseStr, // String accept karega
            @Valid @RequestBody KeycloakRoleRequestDTO roleRequest) {
        try {
            //  Convert String to Boolean
            boolean saveToDatabase = Boolean.parseBoolean(saveToDatabaseStr);

            //  Check if "Create Role" service is available
            boolean isServiceUp = keycloakRoleService.isKeycloakServiceAvailableForCreteRole(realm, clientId, token);

            if (!isServiceUp) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ApiResponse<>(null, false, "SERVICE-DOWN", "Create Role service is down. Try later."));
            }

            //  Proceed if Create Role service is UP
            String responseMessage = keycloakRoleService.createRoleInKeycloak(realm, clientId, roleRequest, token, saveToDatabase);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(responseMessage, true, "ROLE-CREATE-SUCCESS", "Role has been created successfully."));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(null, false, "ROLE-CREATE-ERROR", "Error", List.of(ex.getMessage(), "Please contact support.")));
        }
    }


    @PostMapping("/createRoleInKeycloakAndUpdate/{realm}/{clientId}")
    public ResponseEntity<ApiResponse<String>> createRoleInKeycloakFinal(
            @PathVariable String realm,
            @PathVariable String clientId,
            @RequestHeader("keycloakToken") String token,
            @RequestParam(value = "saveToDatabase", defaultValue = "true") boolean saveToDatabase,
            @RequestParam(value = "localRoleId", required = false) Long localRoleId,
            @Valid @RequestBody KeycloakRoleRequestDTO roleRequest) {
        try {
            // Step 1: Check if Keycloak Role Creation Service is Available
            if (!keycloakRoleService.isKeycloakServiceAvailable(realm, clientId, token)) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ApiResponse<>(null, false, "SERVICE-DOWN", "Keycloak Role service is down."));
            }

            // Step 2: Create Role in Keycloak
            String responseMessage = keycloakRoleService.createRoleTest(realm, clientId, roleRequest, token, saveToDatabase, localRoleId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(responseMessage, true, "ROLE-CREATE-SUCCESS", "Role created successfully."));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "ROLE-CREATE-ERROR", "Error occurred while creating role.", List.of(ex.getMessage())));
        }
    }

}

