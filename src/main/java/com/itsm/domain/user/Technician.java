package com.itsm.domain.user;

import com.itsm.domain.enums.Role;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A support engineer who investigates and resolves incidents.
 *
 * Skills are stored as category codes (e.g. "NETWORK", "DATABASE") so the
 * skill-based assignment strategy can match them against an incident's category.
 *
 * Wrapper types (Integer, Boolean) are used because all user types share one
 * table, so these columns must allow NULL for employees and admins.
 */
@Entity
@DiscriminatorValue("TECHNICIAN")
public class Technician extends User {

    private static final int DEFAULT_MAX_ACTIVE_INCIDENTS = 5;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "technician_skills",
            joinColumns = @JoinColumn(name = "technician_id"))
    @Column(name = "skill", length = 50)
    private Set<String> skills = new HashSet<>();

    private Integer experienceYears;

    private Integer maxActiveIncidents;

    private Boolean available;

    protected Technician() {
    }

    public Technician(String username, String passwordHash, String fullName,
                      String email, int experienceYears) {
        super(username, passwordHash, fullName, email);
        if (experienceYears < 0) {
            throw new IllegalArgumentException("experienceYears must not be negative");
        }
        this.experienceYears = experienceYears;
        this.maxActiveIncidents = DEFAULT_MAX_ACTIVE_INCIDENTS;
        this.available = true;
    }

    @Override
    public Role getRole() {
        return Role.TECHNICIAN;
    }

    // ---------- skills ----------

    public void addSkill(String skill) {
        skills.add(normalize(skill));
    }

    public void removeSkill(String skill) {
        skills.remove(normalize(skill));
    }

    public boolean hasSkill(String skill) {
        return skill != null && skills.contains(normalize(skill));
    }

    /** Read-only view, so callers cannot change skills without going through addSkill/removeSkill. */
    public Set<String> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    // ---------- availability and workload ----------

    public boolean isAvailable() {
        return Boolean.TRUE.equals(available);
    }

    public void markAvailable() {
        this.available = true;
    }

    public void markUnavailable() {
        this.available = false;
    }

    public int getMaxActiveIncidents() {
        return maxActiveIncidents == null ? DEFAULT_MAX_ACTIVE_INCIDENTS : maxActiveIncidents;
    }

    public void changeMaxActiveIncidents(int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("maxActiveIncidents must be greater than 0");
        }
        this.maxActiveIncidents = max;
    }

    /** True if this technician can take one more incident given their current active count. */
    public boolean canTakeMore(int currentActiveIncidents) {
        return isAvailable() && currentActiveIncidents < getMaxActiveIncidents();
    }

    public int getExperienceYears() {
        return experienceYears == null ? 0 : experienceYears;
    }

    public void changeExperienceYears(int years) {
        if (years < 0) {
            throw new IllegalArgumentException("experienceYears must not be negative");
        }
        this.experienceYears = years;
    }

    private static String normalize(String skill) {
        return requireText(skill, "skill").toUpperCase(Locale.ROOT);
    }
}
