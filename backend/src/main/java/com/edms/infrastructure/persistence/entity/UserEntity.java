package com.edms.infrastructure.persistence.entity;

import com.edms.domain.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "cognito_sub", length = 255, unique = true)
    private String cognitoSub;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 255, nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 50, nullable = false)
    private UserRole role;

    @Column(length = 255)
    private String department;

    @Column(name = "department_id", length = 64)
    private String departmentId;

    @Column(length = 512)
    private String avatar;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
