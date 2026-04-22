package woodwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication
// tells swager to build documentation and apply security globally
@OpenAPIDefinition(
        info = @Info(title = "Woodwork Inventory API", version = "1.0", description = "Core business logic for products and categories"),
        security = @SecurityRequirement(name = "Bearer Authentication")
)
// defines what bearer authentication means(jwt token)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@EnableAsync
@EnableScheduling
public class WoodworkApplication {
    public static void main(String[] args) {
        SpringApplication.run(WoodworkApplication.class, args);
    }
}
