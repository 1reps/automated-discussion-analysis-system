package com.adas.presentation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger UI 설정.
 * - Swagger UI: /docs
 * - API 문서: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI adasOpenAPI() {
    return new OpenAPI()
        .components(new Components())
        .info(new Info()
            .title("ADAS API Gateway")
            .description("Spring Boot API that orchestrates STT and Diarization microservices.")
            .version("0.1.0")
            .contact(new Contact().name("ADAS").email("dev@example.com"))
            .license(new License().name("Proprietary")))
        .externalDocs(new ExternalDocumentation().description("Project README"));
  }

  @Bean
  public GroupedOpenApi apiV1Group() {
    return GroupedOpenApi.builder()
        .group("api-v1")
        .pathsToMatch("/api/**")
        .build();
  }
}

