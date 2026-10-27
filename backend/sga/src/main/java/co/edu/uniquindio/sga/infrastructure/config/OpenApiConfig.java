package co.edu.uniquindio.sga.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Agrega el botón "Authorize" en Swagger UI: se pega el token del login y
 * Swagger lo envía como Authorization: Bearer <token> en cada petición.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "SGA - San Rocco", version = "1.0"),
        security = @SecurityRequirement(name = "bearer"))
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
