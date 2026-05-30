package com.technokratos.agona.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MockInterview API")
                        .description("REST API платформы для подготовки к техническим интервью. " +
                                "Аутентификация выполняется через сессионный cookie JSESSIONID (форма /login).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MockInterview")
                                .url("http://localhost:8081")))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Сессионный cookie, выдаётся после успешного POST /login")));
    }
}
