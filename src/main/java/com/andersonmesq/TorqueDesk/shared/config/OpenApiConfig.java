package com.andersonmesq.TorqueDesk.shared.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
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
    public GroupedOpenApi customerApi(){
        return GroupedOpenApi.builder()
                .group("3 - Customer")
                .pathsToMatch("/api/v1/customer/**")
                .build();
    }

    @Bean
    public GroupedOpenApi serviceApi(){
        return GroupedOpenApi.builder()
                .group("4 - Service")
                .pathsToMatch("/api/v1/service/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi(){
        return GroupedOpenApi.builder()
                .group("5 - User")
                .pathsToMatch("/api/v1/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userTenantApi(){
        return GroupedOpenApi.builder()
                .group("6 - User-tenants")
                .pathsToMatch("/api/v1/user-tenants/**")
                .build();
    }

    @Bean
    public GroupedOpenApi vehicleApi(){
        return GroupedOpenApi.builder()
                .group("7 - Vehicles")
                .pathsToMatch("/api/v1/vehicles/**")
                .build();
    }
}