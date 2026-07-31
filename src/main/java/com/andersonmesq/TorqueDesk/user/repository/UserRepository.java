package com.andersonmesq.TorqueDesk.user.repository;

import com.andersonmesq.TorqueDesk.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query(
            """
                    select u from User u where
                    u.email = :login or u.userName = :login
                    """
    )
    Optional<User> findByLogin(String login);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

    boolean existsByEmail(String email);
}