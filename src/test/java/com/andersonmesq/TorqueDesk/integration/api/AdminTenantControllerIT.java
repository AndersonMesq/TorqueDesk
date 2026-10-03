package com.andersonmesq.TorqueDesk.integration.api;

import com.andersonmesq.TorqueDesk.admin.controller.AdminTenantController;
import com.andersonmesq.TorqueDesk.admin.service.AdminTenantService;
import com.andersonmesq.TorqueDesk.admin.service.TenantProvisioningService;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class AdminTenantControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AdminTenantService adminTenantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        tenantRepository.deleteAll();
    }


}