package com.atomicnorth.hrm.tenant.web.rest.keycloak;

import com.atomicnorth.hrm.tenant.service.dto.keycloak.KeycloakTokenRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.keycloak.KeycloakTokenResponseDTO;
import com.atomicnorth.hrm.tenant.service.keycloak.KeycloakAuthService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/authKeycloak")
public class KeycloakAuthController {

    private final KeycloakAuthService keycloakAuthService;

    public KeycloakAuthController(KeycloakAuthService keycloakAuthService) {
        this.keycloakAuthService = keycloakAuthService;
    }


    @PostMapping("/token")
    public ResponseEntity<Mono<ApiResponse<KeycloakTokenResponseDTO>>> getToken(@RequestBody KeycloakTokenRequestDTO request) {
        try {
            Mono<ApiResponse<KeycloakTokenResponseDTO>> tokenResponse = keycloakAuthService.getToken(request)
                    .map(response -> new ApiResponse<>(
                            response,
                            true,
                            "TOKEN-GENERATION-SUCCESS",
                            "Information",
                            null
                    ));

            return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
        } catch (Exception ex) {
            ApiResponse<KeycloakTokenResponseDTO> errorResponse = new ApiResponse<>(
                    null,
                    false,
                    "TOKEN-GENERATION-FAILURE",
                    "Error",
                    List.of(ex.getMessage(), "Please contact support.")
            );
            return ResponseEntity.status(HttpStatus.OK).body(Mono.just(errorResponse));
        }
    }


}


