package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String INIT_DATA_SCHEME = "TelegramInitData";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Shop Telegram Mini App API")
                .version("1.0.0")
                .description("REST API для Telegram Mini App — планировщик питания для пар."))
            .components(new Components()
                .addSecuritySchemes(INIT_DATA_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Telegram-Init-Data")))
            .addSecurityItem(new SecurityRequirement().addList(INIT_DATA_SCHEME));
    }
}
