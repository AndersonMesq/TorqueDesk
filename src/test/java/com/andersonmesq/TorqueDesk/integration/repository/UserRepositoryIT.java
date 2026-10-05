package com.andersonmesq.TorqueDesk.integration.repository;

import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistUser() {

    }

    @Test
    void shouldFindUserByLoginSuccessfully() {

    }

    @Test
    void shouldRejectWhenLoginIsInvalid() {

    }

    @Test
    void shouldFindByEmailSuccessfully() {

    }

    @Test
    void shouldRejectWhenEmailNotExists() {

    }

    @Test
    void shouldReturnTrueWhenEmailExists() {

    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExists() {

    }

    @Test
    void shouldReturnTrueWhenUserNameExists() {

    }

    @Test
    void shouldReturnFalseWhenUserNameDoesNotExists() {

    }

    @Test
    void shouldReturnTrueWhenUserNameAndEmailExists() {

    }

    @Test
    void shouldReturnFalseWhenUserNameAndEmailDoesNotExists() {

    }
}
