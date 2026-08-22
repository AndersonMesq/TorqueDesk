package com.andersonmesq.TorqueDesk.shared.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TorqueDesk API")
                        .description("API for workshop management")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi authenticationApi(){
        return GroupedOpenApi.builder()
                .group("0 - Authentication")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi tenantAdminApi(){
        return GroupedOpenApi.builder()
                .group("1 - Tenant Admin")
                .pathsToMatch("/api/v1/admin/tenants/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userAdminApi(){
        return GroupedOpenApi.builder()
                .group("2 - User Admin")
                .pathsToMatch("/api/v1/admin/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userTenantApi(){
        return GroupedOpenApi.builder()
                .group("3 - User-tenants")
                .pathsToMatch("/api/v1/user-tenants/**")
                .build();
    }

    @Bean
    public GroupedOpenApi customerApi(){
        return GroupedOpenApi.builder()
                .group("4 - Customers")
                .pathsToMatch("/api/v1/customers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi vehicleApi(){
        return GroupedOpenApi.builder()
                .group("5 - Vehicles")
                .pathsToMatch("/api/v1/vehicles/**")
                .build();
    }

    @Bean
    public GroupedOpenApi serviceApi(){
        return GroupedOpenApi.builder()
                .group("6 - Services")
                .pathsToMatch("/api/v1/services/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi(){
        return GroupedOpenApi.builder()
                .group("7 - Users")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }
}