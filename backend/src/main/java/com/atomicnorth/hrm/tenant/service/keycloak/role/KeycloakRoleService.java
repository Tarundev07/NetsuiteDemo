package com.atomicnorth.hrm.tenant.service.keycloak.role;

import com.atomicnorth.hrm.configuration.WebConfigurer;
import com.atomicnorth.hrm.tenant.domain.roles.Role;
import com.atomicnorth.hrm.tenant.repository.roles.RoleRepository;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.role.KeycloakRoleRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.role.KeycloakRoleResponseDTO;
import com.atomicnorth.hrm.tenant.service.translation.SupraTranslationCommonServices;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakRoleService {

    private final WebClient webClient;
    private final Logger logger = LoggerFactory.getLogger(WebConfigurer.class);
    @Autowired
    private SupraTranslationCommonServices supraTranslationCommonServices;
    @Autowired
    private RoleRepository roleRepository;


    @Autowired
    public KeycloakRoleService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://103.16.222.73:809")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String createRole(String realm, String clientId, KeycloakRoleRequestDTO roleRequest, String accessToken) {
        String url = String.format("/admin/realms/%s/clients/%s/roles", realm, clientId);

        log.info("Calling Keycloak API: {}", "http://103.16.222.73:809" + url);

        try {
            ResponseEntity<String> response = webClient.post()
                    .uri("http://103.16.222.73:809" + url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(roleRequest)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            log.info("Keycloak API Response: {}", response);

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return "Role created successfully";
            } else if (response != null && response.getStatusCode() == HttpStatus.CONFLICT) {
                throw new WebClientResponseException(HttpStatus.CONFLICT.value(), "Role already exists", null, null, null);
            } else {
                String errorBody = response != null ? response.getBody() : "Unknown error";
                throw new WebClientResponseException(
                        response != null ? response.getStatusCode().value() : 500,
                        "Keycloak Error",
                        null,
                        errorBody.getBytes(),
                        null
                );
            }
        } catch (WebClientResponseException e) {
            log.error("Keycloak API Error: {}", e.getMessage());
            throw e;
        }
    }

    public Mono<List<KeycloakRoleResponseDTO>> getAllRoles(
            String realm, String clientId, String accessToken, String searchColumn, String searchValue, String sortBy, String sortDir) {

        String url = String.format("/admin/realms/%s/clients/%s/roles", realm, clientId);

        return webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToFlux(KeycloakRoleResponseDTO.class)
                .collectList()
                .map(roles -> {
                    // Filtering logic
                    if (searchColumn != null && searchValue != null && !searchValue.isEmpty()) {
                        roles = roles.stream()
                                .filter(role -> matchesSearchCriteria(role, searchColumn, searchValue))
                                .collect(Collectors.toList());
                    }

                    // Sorting logic
                    return roles.stream()
                            .sorted((r1, r2) -> {
                                try {
                                    Field field = KeycloakRoleResponseDTO.class.getDeclaredField(sortBy);
                                    field.setAccessible(true);
                                    String value1 = String.valueOf(field.get(r1));
                                    String value2 = String.valueOf(field.get(r2));

                                    return sortDir.equalsIgnoreCase("asc") ?
                                            value1.compareToIgnoreCase(value2) :
                                            value2.compareToIgnoreCase(value1);
                                } catch (Exception e) {
                                    return 0;
                                }
                            })
                            .collect(Collectors.toList());
                });
    }


    private boolean matchesSearchCriteria(KeycloakRoleResponseDTO role, String searchColumn, String searchValue) {
        switch (searchColumn.toLowerCase()) {
            case "id":
                return role.getId().equalsIgnoreCase(searchValue);
            case "name":
                return role.getName().toLowerCase().contains(searchValue.toLowerCase());
            case "description":
                return role.getDescription() != null && role.getDescription().toLowerCase().contains(searchValue.toLowerCase());
            case "composite":
                return Boolean.toString(role.isComposite()).equalsIgnoreCase(searchValue);
            case "clientrole":
                return Boolean.toString(role.isClientRole()).equalsIgnoreCase(searchValue);
            case "containerid":
                return role.getContainerId().equalsIgnoreCase(searchValue);
            default:
                return false;
        }
    }


    /*TEST */


    @Transactional(rollbackFor = Exception.class) //  Full rollback if Keycloak or DB fails
    public String createRealmRoleTest(String realm, KeycloakRoleRequestDTO roleRequest, String accessToken) {
        String keycloakUrl = String.format("http://103.16.222.73:809/admin/realms/%s/roles", realm);
        log.info("Calling Keycloak API: {}", keycloakUrl);

        try {
            ResponseEntity<String> response = webClient.post()
                    .uri(keycloakUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(roleRequest)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                //  Step 1: Fetch Role ID from Keycloak
                String roleId = fetchRoleIdFromKeycloak(realm, roleRequest.getName(), accessToken);

                //  Step 2: Generate Clean & Unique Codes
                String baseRoleName = roleRequest.getName().trim().replaceAll("\\s+", "_").toUpperCase();
                String shortUUID = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // Short Unique Key
                String generatedCode = baseRoleName + "_SSO_" + shortUUID;
                String generatedDescriptionCode = baseRoleName + "_DESC_" + shortUUID;

                //  Step 3: Save Role in DB
                Role role = new Role();
                role.setRoleCode(baseRoleName);
                role.setRoleNameCode(generatedCode);
                role.setRoleDescriptionCode(generatedDescriptionCode);
                role.setSso_role_unique_key(roleId);
                role.setSso_role_activation("ACTIVE");
                role.setCreatedBy("System");
                role.setCreationDate(new Date());

                //  Step 4: Insert Translation Data
                Map<String, String> translationDataMap = new HashMap<>();
                translationDataMap.put(generatedCode, roleRequest.getName().trim());
                translationDataMap.put(generatedDescriptionCode, roleRequest.getDescription().trim());

                boolean translationSuccess = supraTranslationCommonServices.saveTranslationData(translationDataMap);
                if (!translationSuccess) {
                    throw new RuntimeException("Translation failed");
                }

                log.info("Saving role to database: {}", role);
                Role savedRole = roleRepository.save(role);

                if (savedRole.getRoleId() == null) {
                    throw new RuntimeException("Failed to save role in the database.");
                }

                return "Role created successfully in Keycloak and stored in DB.";
            } else if (response != null && response.getStatusCode() == HttpStatus.CONFLICT) {
                throw new WebClientResponseException(HttpStatus.CONFLICT.value(), "Role already exists", null, null, null);
            } else {
                throw new WebClientResponseException(
                        response != null ? response.getStatusCode().value() : 500,
                        "Keycloak Error",
                        null,
                        response != null ? response.getBody().getBytes() : new byte[0],
                        null
                );
            }
        } catch (WebClientResponseException e) {
            log.error("Keycloak API Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected Error: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating role: " + e.getMessage());
        }
    }


    //  Fetch Role ID from Keycloak
    private String fetchRoleIdFromKeycloak(String realm, String roleName, String accessToken) {
        String roleUrl = String.format("http://103.16.222.73:809/admin/realms/%s/roles/%s", realm, roleName);

        ResponseEntity<JsonNode> response = webClient.get()
                .uri(roleUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toEntity(JsonNode.class)
                .block();

        if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().get("id").asText();
        }

        throw new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "Role ID not found in Keycloak", null, null, null);
    }


    @Transactional(rollbackFor = Exception.class)
    public String createRoleInKeycloak(String realm, String clientId, KeycloakRoleRequestDTO roleRequest, String accessToken, boolean saveToDatabase) {
        String keycloakUrl = String.format("http://103.16.222.73:809/admin/realms/%s/clients/%s/roles", realm, clientId);

        //  First, check if Keycloak's Create Role API is UP
        if (!isKeycloakServiceAvailableForCreteRole(realm, clientId, accessToken)) {
            return "Keycloak Create Role service is DOWN. Please try again later.";
        }

        //  Call Keycloak API to Create Role
        ResponseEntity<String> response = webClient.post()
                .uri(keycloakUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(roleRequest)
                .retrieve()
                .toEntity(String.class)
                .block();

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create role in Keycloak. Response: " + (response != null ? response.getBody() : "No response"));
        }

        //  Fetch Role ID from Keycloak
        String roleId = fetchRoleIdFromKeycloak(realm, clientId, roleRequest.getName(), accessToken);

        //  If saveToDatabase = true, store role locally
        if (saveToDatabase) {
            String baseRoleName = roleRequest.getName().trim().replaceAll("\\s+", "_").toUpperCase();
            String shortUUID = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String generatedCode = baseRoleName + "_SSO_" + shortUUID;
            String generatedDescriptionCode = baseRoleName + "_DESC_" + shortUUID;

            Role role = new Role();
            role.setRoleCode(baseRoleName);
            role.setRoleNameCode(generatedCode);
            role.setRoleDescriptionCode(generatedDescriptionCode);
            role.setSso_role_unique_key(roleId);
            role.setSso_role_activation("ACTIVE");
            role.setCreatedBy("System");
            role.setCreationDate(new Date());

            //  Save translations
            Map<String, String> translationDataMap = new HashMap<>();
            translationDataMap.put(generatedCode, roleRequest.getName().trim());
            translationDataMap.put(generatedDescriptionCode, roleRequest.getDescription().trim());

            boolean translationSuccess = supraTranslationCommonServices.saveTranslationData(translationDataMap);
            if (!translationSuccess) {
                throw new RuntimeException("Translation failed");
            }

            //  Save Role in DB
            Role savedRole = roleRepository.save(role);
            if (savedRole.getRoleId() == null) {
                throw new RuntimeException("Failed to save role in database.");
            }
        }

        return "Role created successfully in Keycloak" + (saveToDatabase ? " and stored in DB." : ".");
    }


    private String fetchRoleIdFromKeycloak(String realm, String clientId, String roleName, String accessToken) {
        String roleUrl = String.format("http://103.16.222.73:809/admin/realms/%s/clients/%s/roles/%s", realm, clientId, roleName);

        ResponseEntity<JsonNode> response = webClient.get()
                .uri(roleUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toEntity(JsonNode.class)
                .block();

        if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().get("id").asText();
        }

        throw new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "Role ID not found in Keycloak", null, null, null);
    }

    public boolean isKeycloakServiceAvailableForCreteRole(String realm, String clientId, String accessToken) {
        String keycloakUrl = String.format("http://103.16.222.73:809/admin/realms/%s/clients/%s/roles", realm, clientId);

        //  Ensure the token has "Bearer " prefix
        if (!accessToken.startsWith("Bearer ")) {
            accessToken = "Bearer " + accessToken;
        }

        try {
            ResponseEntity<String> response = webClient.get()
                    .uri(keycloakUrl)
                    .header(HttpHeaders.AUTHORIZATION, accessToken) //  Ensure token is set
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            //  If response is 200, service is UP
            return response != null && response.getStatusCode().is2xxSuccessful();
        } catch (WebClientResponseException ex) {
            System.out.println("Keycloak Service Down: " + ex.getMessage());
            return false; //  Service is down or unauthorized
        } catch (Exception e) {
            System.out.println("Unexpected Error Checking Keycloak Service: " + e.getMessage());
            return false; //  Unexpected error
        }
    }


    /*test*/

    @Transactional(rollbackFor = Exception.class)
    public String createRoleTest(String realm, String clientId, KeycloakRoleRequestDTO roleRequest, String accessToken, boolean saveToDatabase, Long localRoleId) {
        String keycloakUrl = String.format("http://103.16.222.73:809/admin/realms/%s/clients/%s/roles", realm, clientId);

        // Step 1: Call Keycloak API to Create Role
        ResponseEntity<String> response = webClient.post()
                .uri(keycloakUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(roleRequest)
                .retrieve()
                .toEntity(String.class)
                .block();

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create role in Keycloak.");
        }

        // Step 2: Fetch Keycloak Role ID
        String keycloakRoleId = fetchRoleIdFromKeycloaks(realm, clientId, roleRequest.getName(), accessToken);
        if (keycloakRoleId == null) {
            throw new RuntimeException("Role created in Keycloak but failed to fetch Role ID.");
        }

        // Step 3: Update Local Database if needed
        if (saveToDatabase && localRoleId != null) {
            Long localRoleId1 = localRoleId;
            Optional<Role> roleOptional = roleRepository.findById(Math.toIntExact(localRoleId1));
            if (roleOptional.isPresent()) {
                Role role = roleOptional.get();
                role.setSso_role_unique_key(keycloakRoleId);
                roleRepository.save(role);
                return "Role created in Keycloak and local DB updated.";
            } else {
                throw new RuntimeException("Local Role ID not found in database.");
            }
        }

        return "Role created successfully in Keycloak.";
    }

    private String fetchRoleIdFromKeycloaks(String realm, String clientId, String roleName, String accessToken) {
        String roleUrl = String.format("http://103.16.222.73:809/admin/realms/%s/clients/%s/roles/%s", realm, clientId, roleName);

        ResponseEntity<JsonNode> response = webClient.get()
                .uri(roleUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toEntity(JsonNode.class)
                .block();

        if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().get("id").asText();
        }

        throw new RuntimeException("Role ID not found in Keycloak.");
    }

    public boolean isKeycloakServiceAvailable(String realm, String clientId, String accessToken) {
        String keycloakUrl = String.format("http://103.16.222.73:809/admin/realms/%s/clients/%s/roles", realm, clientId);

        try {
            ResponseEntity<String> response = webClient.get()
                    .uri(keycloakUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response != null && response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }


}
