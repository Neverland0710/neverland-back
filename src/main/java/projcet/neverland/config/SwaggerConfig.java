package projcet.neverland.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());}
    private Info apiInfo() {
        return new Info()
                .title("neverland API 명세서")
                .description("neverland API 명세서을 사용한 Swagger UI")
                .version("1.0.0");}}
