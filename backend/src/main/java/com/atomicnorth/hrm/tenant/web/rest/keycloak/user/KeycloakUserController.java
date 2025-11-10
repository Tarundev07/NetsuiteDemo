package com.atomicnorth.hrm.tenant.web.rest.keycloak.user;


import com.atomicnorth.hrm.tenant.service.dto.keycloak.user.KeycloakUserRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.user.KeycloakUserResponseDTO;
import com.atomicnorth.hrm.tenant.service.keycloak.user.KeycloakUserService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/KeycloakUser")
public class KeycloakUserController {

    private final KeycloakUserService keycloakUserService;


    public KeycloakUserController(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }

    @GetMapping("/users/{realm}")
    public ResponseEntity<Mono<ApiResponse<List<KeycloakUserResponseDTO>>>> getUsers(
            @PathVariable String realm,
            @RequestHeader("token") String token) {
        try {
            // Removing "Bearer " prefix from token
            String accessToken = token.replace("Bearer ", "");

            Mono<List<KeycloakUserResponseDTO>> users = keycloakUserService.getUsers(realm, accessToken);

            ApiResponse<List<KeycloakUserResponseDTO>> response = new ApiResponse<>(
                    users.block(),
                    true,
                    "USER-FETCH-SUCCESS",
                    "Fetched users successfully"
            );

            return ResponseEntity.status(HttpStatus.OK).body(Mono.just(response));
        } catch (Exception ex) {
            ApiResponse<List<KeycloakUserResponseDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "USER-FETCH-FAILURE",
                    "Error fetching users",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.OK).body(Mono.just(errorResponse));
        }
    }


    @GetMapping("/getAllUsers/{realm}")
    public ResponseEntity<Mono<ApiResponse<List<KeycloakUserResponseDTO>>>> getUsers(
            @PathVariable String realm,
            @RequestHeader("token") String token,
            @RequestParam(required = false) String searchColumn,
            @RequestParam(required = false) String searchValue,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            String accessToken = token.replace("Bearer ", "");

            Mono<List<KeycloakUserResponseDTO>> users = keycloakUserService.getAllUsers(
                    realm, accessToken, searchColumn, searchValue, sortBy, sortDir);

            ApiResponse<List<KeycloakUserResponseDTO>> response = new ApiResponse<>(
                    users.block(),
                    true,
                    "USER-FETCH-SUCCESS",
                    "Fetched users successfully"
            );

            return ResponseEntity.status(HttpStatus.OK).body(Mono.just(response));
        } catch (Exception ex) {
            ApiResponse<List<KeycloakUserResponseDTO>> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "USER-FETCH-FAILURE",
                    "Error fetching users",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Mono.just(errorResponse));
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


    @PostMapping("/createUsers/{realm}")
    public ResponseEntity<ApiResponse<String>> createUser(
            @PathVariable String realm,
            @RequestHeader("keycloakToken") String keycloakToken,
            @RequestParam(value = "saveToDatabase", defaultValue = "true") boolean saveToDatabase,
            @Valid @RequestBody KeycloakUserRequestDTO userRequest) {
        try {
            //  Step 1: Check if Keycloak is Up
            if (!keycloakUserService.isKeycloakUp(realm, keycloakToken)) {
                throw new RuntimeException("Keycloak is down. Cannot create user.");
            }

            //  Step 2: Create User
            String responseMessage = keycloakUserService.createUsers(realm, userRequest, keycloakToken, saveToDatabase);

            //  Step 3: Success Response
            ApiResponse<String> response = new ApiResponse<>(
                    responseMessage, true, "USER-CREATE-SUCCESS", "User has been created successfully."
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (WebClientResponseException ex) {
            String errorMessage = extractKeycloakErrorMessage(ex);
            return buildErrorResponse("USER-CREATE-FAILURE", "Failed to create user: " + errorMessage, ex.getStatusCode());

        } catch (Exception ex) {
            return buildErrorResponse("USER-CREATE-ERROR", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<ApiResponse<String>> buildErrorResponse(String errorCode, String errorMessage, HttpStatus status) {
        ApiResponse<String> errorResponse = new ApiResponse<>(
                null, false, errorCode, "Error", List.of(errorMessage, "Please contact support.")
        );
        return ResponseEntity.status(status).body(errorResponse);
    }


    @PostMapping("/createUserAfterLocalApi/{realm}")
    public ResponseEntity<ApiResponse<String>> createUserOnKeyClockAfterLocalApi(
            @PathVariable String realm,
            @RequestHeader("keycloakToken") String keycloakToken,
            @RequestParam("localUserId") Long localUserId,
            @Valid @RequestBody KeycloakUserRequestDTO userRequest) {
        try {
            String keycloakUserId = keycloakUserService.createUserAndUpdateLocalDB(realm, userRequest, keycloakToken, localUserId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(keycloakUserId, true, "USER-CREATE-SUCCESS",
                            "User created successfully in Keycloak. Keycloak User ID: " + keycloakUserId));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, "USER-CREATE-ERROR", ex.getMessage()));
        }
    }
}




