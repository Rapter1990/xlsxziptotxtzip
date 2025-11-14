package com.example.xlsxziptotxtzip.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Configuration class for setting up OpenAPI documentation for the application.
 * This class configures the metadata for the API documentation using OpenAPI 3.0 annotations.
 */
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Sercan Noyan Germiyanoğlu",
                        url = "https://github.com/Rapter1990/xlsxziptotxtzip"
                ),
                description = "Case Study - Xlsx Zip To Txt Zip" +
                        " (Java 25, Spring Boot, Mysql, JUnit, Docker, Kubernetes, Prometheus, Grafana, Github Actions (CI/CD), Jenkins) ",
                title = "xlsxziptotxtzip",
                version = "1.0.0"
        )
)
public class OpenApiConfig {

}
