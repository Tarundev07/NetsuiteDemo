package com.atomicnorth.hrm.tenant.service.keycloak.user;

import com.atomicnorth.hrm.configuration.WebConfigurer;
import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.service.UserService;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.user.KeycloakUserRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.user.KeycloakUserResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakUserService {

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(WebConfigurer.class);


    public KeycloakUserService(WebClient.Builder webClientBuilder, UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
        this.webClient = webClientBuilder.baseUrl("http://103.16.222.73:809").build();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }


    public Mono<List<KeycloakUserResponseDTO>> getUsers(String realm, String accessToken) {
        String url = String.format("/admin/realms/%s/users", realm);

        return webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToFlux(KeycloakUserResponseDTO.class)
                .collectList();
    }


    public Mono<List<KeycloakUserResponseDTO>> getAllUsers(
            String realm, String accessToken, String searchColumn, String searchValue, String sortBy, String sortDir) {
        String url = String.format("/admin/realms/%s/users", realm);

        return webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToFlux(KeycloakUserResponseDTO.class)
                .collectList()
                .map(users -> {
                    // Filtering logic
                    if (searchColumn != null && searchValue != null && !searchValue.isEmpty()) {
                        users = users.stream()
                                .filter(user -> matchesSearchCriteria(user, searchColumn, searchValue))
                                .collect(Collectors.toList());
                    }

                    // Sorting logic
                    return users.stream()
                            .sorted((u1, u2) -> {
                                try {
                                    Field field = KeycloakUserResponseDTO.class.getDeclaredField(sortBy);
                                    field.setAccessible(true);
                                    String value1 = String.valueOf(field.get(u1));
                                    String value2 = String.valueOf(field.get(u2));

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

    /*
     * Helper method to check if a user matches search criteria.
     */
    private boolean matchesSearchCriteria(KeycloakUserResponseDTO user, String searchColumn, String searchValue) {
        switch (searchColumn.toLowerCase()) {
            case "id":
                return user.getId().equalsIgnoreCase(searchValue);
            case "username":
                return user.getUsername().toLowerCase().contains(searchValue.toLowerCase());
            case "email":
                return user.getEmail().toLowerCase().contains(searchValue.toLowerCase());
            case "firstname":
                return user.getFirstName().toLowerCase().contains(searchValue.toLowerCase());
            case "lastname":
                return user.getLastName().toLowerCase().contains(searchValue.toLowerCase());
            default:
                return false;
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public String createUsers(String realm, KeycloakUserRequestDTO userRequest, String accessToken, boolean saveToDatabase) {
        String url = String.format("/admin/realms/%s/users", realm);
        logger.info("Calling Keycloak API: {}", url);

        try {
            //  Step 1: Keycloak Health Check
            if (!isKeycloakUp(realm, accessToken)) {
                throw new RuntimeException("Keycloak is down. Cannot create user.");
            }

            //  Step 2: Create User in Keycloak
            ResponseEntity<Void> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(userRequest)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new WebClientResponseException(response.getStatusCode().value(), "Keycloak Error", null, null, null);
            }

            logger.info("User created successfully in Keycloak");

            //  Step 3: Fetch Keycloak User ID
            Thread.sleep(2000);
            String keycloakId = getKeycloakUserIds(realm, userRequest.getUsername(), accessToken);
            if (keycloakId == null) {
                throw new RuntimeException("User created in Keycloak but failed to fetch Keycloak ID.");
            }

            //  Step 4: Save User to Database (Only if Needed)
            if (saveToDatabase) {
                saveUserToDatabase(userRequest, keycloakId);
            }

            return "User created successfully in Keycloak and database";
        } catch (WebClientResponseException e) {
            logger.error("Keycloak API Error: {}", e.getMessage());
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted while waiting for Keycloak", e);
            throw new RuntimeException("Thread interruption error", e);
        }
    }

    private void saveUserToDatabase(KeycloakUserRequestDTO userRequest, String keycloakId) {
        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setDisplayName(userRequest.getFirstName() + " " + userRequest.getLastName());
        user.setKeycloakId(keycloakId);
        user.setKeycloakUserActivation("true"); // Maintain activation status
        user.setStartDate(new Date());
        user.setEndDate(null);
        user.setIsActive("Y");
        user.setActivated(true);

        //  Fix: Ensure Mobile Number Can Be Null
        // user.setMobileNumber(userRequest.getMobileNumber() != null ? userRequest.getMobileNumber() : null);

        //  Encrypt password before saving
        if (userRequest.getCredentials() != null && !userRequest.getCredentials().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getCredentials().get(0).getValue()));
        }

        //  Save user in the database
        userRepository.save(user);
        logger.info("User saved successfully in the database with Keycloak ID: {}", keycloakId);
    }

    public boolean isKeycloakUp(String realm, String accessToken) {
        String healthCheckUrl = String.format("/admin/realms/%s", realm);

        try {
            ResponseEntity<Void> response = webClient.get()
                    .uri(healthCheckUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.error("Keycloak health check failed: {}", e.getMessage());
            return false;
        }
    }

    private String getKeycloakUserIds(String realm, String username, String accessToken) {
        String url = String.format("/admin/realms/%s/users?username=%s", realm, username);

        try {
            ResponseEntity<String> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                if (jsonNode.isArray() && jsonNode.size() > 0) {
                    return jsonNode.get(0).get("id").asText();
                } else {
                    logger.error("Keycloak user list is empty. User might not have been created.");
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching Keycloak user ID", e);
        }
        return null;
    }


    @Transactional(rollbackFor = Exception.class)
    public String createUserAndUpdateLocalDB(String realm, KeycloakUserRequestDTO userRequest, String accessToken, Long localUserId) {
        String url = String.format("/admin/realms/%s/users", realm);

        // Create user in Keycloak
        ResponseEntity<Void> response = webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(userRequest)
                .retrieve()
                .toBodilessEntity()
                .block();

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create user in Keycloak");
        }

        // Fetch Keycloak User ID
        String keycloakUserId = fetchKeycloakUserId(realm, userRequest.getUsername(), accessToken);
        if (keycloakUserId == null) {
            throw new RuntimeException("Failed to fetch Keycloak user ID");
        }

        // Update local database
        User user = userRepository.findById(localUserId)
                .orElseThrow(() -> new RuntimeException("Local user not found"));
        user.setKeycloakId(keycloakUserId);
        userRepository.save(user);

        // Return Keycloak User ID
        return keycloakUserId;
    }

    private String fetchKeycloakUserId(String realm, String username, String accessToken) {
        String url = String.format("/admin/realms/%s/users?username=%s", realm, username);

        ResponseEntity<String> response = webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toEntity(String.class)
                .block();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                return jsonNode.get(0).get("id").asText();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Keycloak response", e);
        }
        return null;
    }

}
