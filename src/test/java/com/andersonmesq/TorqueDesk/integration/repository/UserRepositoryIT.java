package com.andersonmesq.TorqueDesk.integration.repository;

import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        User user = createUser(SystemRole.USER, "johntest", "john.test@email.com");

        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User persistedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(persistedUser.getFullName()).isEqualTo(savedUser.getFullName());
        assertThat(persistedUser.getUserName()).isEqualTo(savedUser.getUserName());
        assertThat(persistedUser.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(persistedUser.getEnabled()).isTrue();
        assertThat(persistedUser.getSystemRole()).isEqualTo(savedUser.getSystemRole());
    }

    @Test
    void shouldPersistUserWithoutUserName() {
        User user = createUser(SystemRole.USER, "", "john.test@email.com");

        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User persistedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(persistedUser.getFullName()).isEqualTo(savedUser.getFullName());
        assertThat(persistedUser.getUserName()).isEmpty();
        assertThat(persistedUser.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(persistedUser.getEnabled()).isTrue();
        assertThat(persistedUser.getSystemRole()).isEqualTo(savedUser.getSystemRole());
    }

    @Test
    void shouldPersistUserWithoutEmail() {
        User user = createUser(SystemRole.USER, "johntest", "");
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User persistedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(persistedUser.getFullName()).isEqualTo(savedUser.getFullName());
        assertThat(persistedUser.getUserName()).isEqualTo(savedUser.getUserName());
        assertThat(persistedUser.getEmail()).isEmpty();
        assertThat(persistedUser.getEnabled()).isTrue();
        assertThat(persistedUser.getSystemRole()).isEqualTo(savedUser.getSystemRole());
    }

    @Test
    void shouldFindUserByLoginWithUserNameSuccessfully() {
        User user = createUser(SystemRole.USER, "johntest", "john.test@email.com");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findByLogin("johntest");

        assertThat(result).isPresent().get().satisfies(findedUser -> {
            assertThat(findedUser.getFullName()).isEqualTo(user.getFullName());
            assertThat(findedUser.getUserName()).isEqualTo(user.getUserName());
            assertThat(findedUser.getEmail()).isEqualTo(user.getEmail());
            assertThat(findedUser.getEnabled()).isTrue();
        });
    }

    @Test
    void shouldRejectWhenLoginWithUserNameNotExists() {
        String nonExistentUserName = "nonExistentUserName";

        Optional<User> user = userRepository.findByLogin(nonExistentUserName);

        assertThat(user).isEmpty();
    }

    @Test
    void shouldFindUserByLoginWithEmailSuccessfully() {
        User user = createUser(SystemRole.USER, "johntest", "john.test@email.com");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findByLogin("john.test@email.com");

        assertThat(result).isPresent().get().satisfies(findedUser -> {
            assertThat(findedUser.getFullName()).isEqualTo(user.getFullName());
            assertThat(findedUser.getUserName()).isEqualTo(user.getUserName());
            assertThat(findedUser.getEmail()).isEqualTo(user.getEmail());
            assertThat(findedUser.getEnabled()).isTrue();
        });
    }

    @Test
    void shouldRejectWhenLoginWithEmailNotExists() {
        String nonExistentEmail = "non.existent@email.com";

        Optional<User> result = userRepository.findByLogin(nonExistentEmail);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindByEmailSuccessfully() {
        User user = createUser(SystemRole.USER, "johntest", "john.test@email.com");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findByEmail(user.getEmail());

        assertThat(result).isPresent().get().satisfies(findedUser -> {
            assertThat(findedUser.getFullName()).isEqualTo(user.getFullName());
            assertThat(findedUser.getUserName()).isEqualTo(user.getUserName());
            assertThat(findedUser.getEmail()).isEqualTo(user.getEmail());
            assertThat(findedUser.getEnabled()).isTrue();
        });
    }

    @Test
    void shouldRejectWhenEmailNotExists() {
        String nonExistentEmail = "non.existent@email.com";

        Optional<User> result = userRepository.findByEmail(nonExistentEmail);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindByUserNameSuccessfully() {
        User user = createUser(SystemRole.USER, "johntest", "john.test@email.com");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findByUserName(user.getUserName());

        assertThat(result).isPresent().get().satisfies(findedUser -> {
            assertThat(findedUser.getFullName()).isEqualTo(user.getFullName());
            assertThat(findedUser.getUserName()).isEqualTo(user.getUserName());
            assertThat(findedUser.getEmail()).isEqualTo(user.getEmail());
            assertThat(findedUser.getEnabled()).isTrue();
        });
    }

    @Test
    void shouldRejectWhenUserNameNotExists() {
        String nonExistentUserName = "nonExistentUserName";

        Optional<User> result = userRepository.findByEmail(nonExistentUserName);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        User user = createUser(SystemRole.USER, "johntest", "john.test@email.com");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        boolean exists = userRepository.existsByEmail(user.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExists() {
        String nonExistentEmail = "non.existent@email.com";

        boolean exists = userRepository.existsByEmail(nonExistentEmail);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenUserNameExists() {
        User user = createUser(SystemRole.USER, "johntest", "john.test@email.com");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        boolean exists = userRepository.existsByUserName(user.getUserName());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserNameDoesNotExists() {
        String nonExistentUserName = "nonExistentUserName";

        boolean exists = userRepository.existsByUserName(nonExistentUserName);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenUserNameAndEmailExists() {
        User user = createUser(SystemRole.USER, "johntest", "john.test@email.com");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        boolean exists = userRepository.existsByUserNameAndEmail(user.getUserName(), user.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserNameAndEmailDoesNotExists() {
        String nonExistentUserName = "nonExistentUserName";
        String nonExistentEmail = "non.existent@email.com";

        boolean exists = userRepository.existsByUserNameAndEmail(nonExistentUserName, nonExistentEmail);

        assertThat(exists).isFalse();
    }

    private User createUser(SystemRole systemRole, String userName, String email) {
        return User.builder()
                .systemRole(systemRole)
                .fullName("John Test")
                .userName(userName)
                .email(email)
                .password("123456789")
                .enabled(true)
                .build();
    }
}