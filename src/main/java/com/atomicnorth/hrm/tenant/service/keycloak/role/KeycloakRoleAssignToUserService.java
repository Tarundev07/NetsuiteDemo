package com.atomicnorth.hrm.tenant.service.keycloak.role;


import com.atomicnorth.hrm.tenant.service.dto.keycloak.role.KeycloakRoleAssignToUserRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.role.KeycloakUserRoleDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Service
public class KeycloakRoleAssignToUserService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;  //  JSON Serializer
    private final PlatformTransactionManager transactionManager; // Manual Transaction Control
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public KeycloakRoleAssignToUserService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, PlatformTransactionManager transactionManager) {
        this.webClient = webClientBuilder.baseUrl("http://103.16.222.73:809").build();
        this.objectMapper = objectMapper;
        this.transactionManager = transactionManager;
    }


    public List<KeycloakUserRoleDTO> getUserRoles(String realm, String userId, String clientId, String accessToken) {
        String url = String.format("/admin/realms/%s/users/%s/role-mappings/clients/%s", realm, userId, clientId);

        log.info("Calling Keycloak API: {}", "http://103.16.222.73:809" + url);

        try {
            ResponseEntity<List<KeycloakUserRoleDTO>> response = webClient.get()
                    .uri("http://103.16.222.73:809" + url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toEntityList(KeycloakUserRoleDTO.class)
                    .block();

            log.info("Keycloak API Response: {}", response);

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new WebClientResponseException(response != null ? response.getStatusCode().value() : 500,
                        "Keycloak Error",
                        null,
                        null,
                        null);
            }
        } catch (WebClientResponseException e) {
            log.error("Keycloak API Error: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isRoleAlreadyAssigned(String realm, String userId, String clientId, List<KeycloakRoleAssignToUserRequestDTO> roles, String token) {
        try {
            List<KeycloakRoleAssignToUserRequestDTO> assignedRoles = webClient.get()
                    .uri("/admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientId}", realm, userId, clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToFlux(KeycloakRoleAssignToUserRequestDTO.class)
                    .collectList()
                    .block();

            if (assignedRoles == null || assignedRoles.isEmpty()) {
                return false;
            }

            // Check if all requested roles are already assigned
            return roles.stream().allMatch(role -> assignedRoles.stream().anyMatch(assigned -> assigned.getId().equals(role.getId())));
        } catch (WebClientResponseException ex) {
            return false; // If Keycloak returns an error, assume the role is not assigned
        }
    }


    public void removeRoleFromUser(String realm, String userId, String clientId, List<KeycloakRoleAssignToUserRequestDTO> roles, String token) {
        webClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientId}", realm, userId, clientId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(roles), List.class)
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    //USE
    public boolean isKeycloakRoleMappingServiceAvailable(String realm, String userId, String clientId, String accessToken) {
        String keycloakUrl = String.format("http://103.16.222.73:809/admin/realms/%s/users/%s/role-mappings/clients/%s",
                realm, userId, clientId);

        //  Ensure token has "Bearer " prefix
        if (!accessToken.startsWith("Bearer ")) {
            accessToken = "Bearer " + accessToken;
        }

        try {
            ResponseEntity<String> response = webClient.get()
                    .uri(keycloakUrl)
                    .header(HttpHeaders.AUTHORIZATION, accessToken) //  Set token correctly
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            //  If response is 200, service is UP
            return response != null && response.getStatusCode().is2xxSuccessful();
        } catch (WebClientResponseException ex) {
            System.out.println("Keycloak Role Mapping Service Down: " + ex.getMessage());
            return false; //  Service is down or unauthorized
        } catch (Exception e) {
            System.out.println("Unexpected Error Checking Role Mapping Service: " + e.getMessage());
            return false; //  Unexpected error
        }
    }


    @Transactional
    public void assignRoleToKeycloak(String realm, String userId, String clientId,
                                     List<KeycloakRoleAssignToUserRequestDTO> roles, String keycloakToken) {

        // Step 1: Check if Role Mapping Service is Available
        if (!isKeycloakRoleMappingServiceAvailable(realm, userId, clientId, keycloakToken)) {
            throw new RuntimeException("Keycloak Role Mapping Service is Down. Try Again Later.");
        }

        // Step 2: Assign Role in Keycloak (Only Keycloak Operation, No DB Changes)
        String keycloakUrl = String.format("http://103.16.222.73:809/admin/realms/%s/users/%s/role-mappings/clients/%s",
                realm, userId, clientId);

        webClient.post()
                .uri(keycloakUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + keycloakToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(roles)
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    public boolean isKeycloakAvailable(String realm, String token) {
        String url = "/realms/" + realm;

        try {
            webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)  //  Pass Token
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();  // Sync call

            return true; // API is working

        } catch (WebClientResponseException ex) {
            return false; // API is down or unauthorized

        } catch (Exception e) {
            return false; // Any unexpected error
        }
    }
}
