package com.itsm.domain.user;

import com.itsm.domain.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Base class for every person who uses the system.
 *
 * OOP notes:
 * - Abstraction: User is abstract; you can only create an Employee, Technician or Admin.
 * - Inheritance: all three are a kind of User ("is-a"), stored in one table (SINGLE_TABLE).
 * - Polymorphism: each subclass answers getRole() differently.
 * - Encapsulation: fields are private and there are no public setters;
 *   changes go through methods that describe the action (changePassword, deactivate, ...).
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Required by JPA. Not for application code. */
    protected User() {
    }

    protected User(String username, String passwordHash, String fullName, String email) {
        this.username = requireText(username, "username");
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.fullName = requireText(fullName, "fullName");
        this.email = requireText(email, "email");
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /** Each type of user reports its own role (polymorphism). */
    public abstract Role getRole();

    // ---------- behaviour ----------

    /** Expects an already-hashed password (BCrypt), never plain text. */
    public void changePassword(String newPasswordHash) {
        this.passwordHash = requireText(newPasswordHash, "passwordHash");
    }

    public void updateProfile(String fullName, String email, String phone) {
        this.fullName = requireText(fullName, "fullName");
        this.email = requireText(email, "email");
        this.phone = phone;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // ---------- getters ----------

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ---------- helpers ----------

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", username='" + username + "'}";
    }
}
