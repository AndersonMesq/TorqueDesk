package com.andersonmesq.TorqueDesk.user.model;

import com.andersonmesq.TorqueDesk.shared.entity.BaseEntity;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemRole systemRole;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(unique = true)
    private String userName;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private Boolean enabled;
}