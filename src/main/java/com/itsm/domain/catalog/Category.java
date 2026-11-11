package com.itsm.domain.catalog;

import jakarta.persistence.*;

import java.util.Locale;

/**
 * An incident category such as NETWORK or DATABASE.
 * Categories are data, not an enum, so admins can add new ones
 * without changing code (requirement: new categories without major changes).
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short unique code, e.g. "NETWORK". Matches technician skills. */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    protected Category() {
    }

    public Category(String code, String name, String description) {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category code and name are required");
        }
        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.name = name.trim();
        this.description = description;
    }

    public void rename(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        this.name = name.trim();
        this.description = description;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
}
