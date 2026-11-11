package com.itsm.domain.catalog;

import com.itsm.domain.enums.BusinessCriticality;
import jakarta.persistence.*;

/** An IT service the organisation depends on, e.g. "Payment Gateway". */
@Entity
@Table(name = "it_services")
public class ItService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BusinessCriticality criticality;

    @Column(nullable = false)
    private boolean active = true;

    protected ItService() {
    }

    public ItService(String name, String description, BusinessCriticality criticality) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Service name is required");
        }
        if (criticality == null) {
            throw new IllegalArgumentException("Service criticality is required");
        }
        this.name = name.trim();
        this.description = description;
        this.criticality = criticality;
    }

    public void update(String name, String description, BusinessCriticality criticality) {
        if (name == null || name.isBlank() || criticality == null) {
            throw new IllegalArgumentException("Service name and criticality are required");
        }
        this.name = name.trim();
        this.description = description;
        this.criticality = criticality;
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BusinessCriticality getCriticality() { return criticality; }
    public boolean isActive() { return active; }
}
