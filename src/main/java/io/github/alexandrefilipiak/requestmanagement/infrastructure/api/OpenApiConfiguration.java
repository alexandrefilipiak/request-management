package io.github.alexandrefilipiak.requestmanagement.infrastructure.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Request Management API")
                .version("0.0.1")
                .description("Manages the lifecycle of requests: creation, verification, acceptance, "
                        + "rejection, deletion and publication, with an audit log of state changes."));
    }
}