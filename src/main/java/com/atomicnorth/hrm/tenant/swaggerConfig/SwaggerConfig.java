package com.atomicnorth.hrm.tenant.swaggerConfig;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile({"dev", "api-docs"})
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "HR-Management system",
                description = "API Documentation",
                version = "1.0.0",
                contact = @Contact(url = "https://example.com"),
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
public class SwaggerConfig {

    @Value("${api.server.local:http://localhost:8080}")
    private String localServerUrl;

    @Value("${api.server.production:http://emanager-saas-uat.supraesapp.com:8080}")
    private String productionServerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new io.swagger.v3.oas.models.servers.Server().url(localServerUrl).description("Local Server"),
                        new io.swagger.v3.oas.models.servers.Server().url(productionServerUrl).description("Production Server")
                ));
    }
}
