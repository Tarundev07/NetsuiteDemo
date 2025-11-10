package com.atomicnorth.hrm.tenant.service.keycloak;

import com.atomicnorth.hrm.tenant.service.dto.keycloak.KeycloakTokenRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.KeycloakTokenResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class KeycloakAuthService {

    private final WebClient webClient;

    public KeycloakAuthService(WebClient webClient) {
        this.webClient = webClient;
    }


    public Mono<KeycloakTokenResponseDTO> getToken(KeycloakTokenRequestDTO request) {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token",
                request.getKeycloakBaseUrl(), request.getRealm());

        // Build Form Data Request
        BodyInserters.FormInserter<String> formData = BodyInserters
                .fromFormData("client_id", request.getClientId())
                .with("grant_type", request.getGrantType());

        // Add username & password if present (for password grant)
        if (Objects.equals(request.getGrantType(), "password")) {
            formData = formData.with("username", request.getUsername())
                    .with("password", request.getPassword());
        }

        // Add client_secret if present (for client_credentials grant)
        if (Objects.equals(request.getGrantType(), "client_credentials") && request.getClientSecret() != null) {
            formData = formData.with("client_secret", request.getClientSecret());
        }

        return webClient.post()
                .uri(url)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(formData)
                .retrieve()
                .bodyToMono(KeycloakTokenResponseDTO.class); //  Corrected: Deserialize response correctly
    }

}
