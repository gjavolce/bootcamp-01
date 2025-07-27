package com.bojan.bootcamp_01.entity;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Schema(description = "User entity representing application users")
public class User {
    @Schema(description = "Unique user identifier", example = "b3b6c1e2-8c2a-4e2a-9b1a-2e3b4c5d6f7a")
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,50}$", message = "Username must be 3-50 characters, alphanumeric, underscore or hyphen")
    @Schema(description = "Username", example = "user_123", required = true)
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid email format")
    @Schema(description = "User email", example = "user@example.com", required = true)
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotNull
    @Schema(description = "Password hash", required = true)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Schema(description = "Is email verified", example = "false")
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Schema(description = "Email verification token")
    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Schema(description = "When verification email was sent")
    @Column(name = "email_verification_sent_at")
    private Instant emailVerificationSentAt;

    @Min(value = 0, message = "Failed login attempts must be >= 0")
    @Max(value = 10, message = "Failed login attempts must be <= 10")
    @Schema(description = "Failed login attempts", example = "0")
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Schema(description = "Account lockout until timestamp")
    @Column(name = "lockout_until")
    private Instant lockoutUntil;

    @Schema(description = "Soft delete timestamp")
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Schema(description = "Created at timestamp")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Schema(description = "Updated at timestamp")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.failedLoginAttempts == 0) {
            this.failedLoginAttempts = 0;
        }
        if (this.emailVerified == false) {
            this.emailVerified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Schema(description = "Flexible metadata as JSONB")
    @Column(name = "metadata")
    private String metadata;
}
