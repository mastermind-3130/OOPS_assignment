package com.itsm.domain.incident;

import com.itsm.domain.catalog.Category;
import com.itsm.domain.catalog.ItService;
import com.itsm.domain.enums.Impact;
import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Priority;
import com.itsm.domain.enums.Urgency;
import com.itsm.domain.user.Employee;
import com.itsm.domain.user.Technician;
import com.itsm.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The central domain object: one reported IT problem.
 *
 * OOP notes:
 * - Association: an incident refers to a reporter, a service, a category and a technician,
 *   which all exist independently of it.
 * - Composition: investigation notes and feedback belong to the incident and are deleted with it
 *   (cascade ALL + orphanRemoval).
 * - Encapsulation: no public setters. Changes go through methods named after business actions.
 *
 * Lifecycle rules (which status may follow which) belong to the State pattern classes in
 * com.itsm.state. Those classes call changeStatus(); other code should go through them.
 */
@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Employee reporter;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ItService service;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Impact impact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Urgency urgency;

    @Column(nullable = false)
    private int affectedUsers;

    @Enumerated(EnumType.STRING)
    @Column(length = 5)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.NEW;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Technician assignedTechnician;

    // ----- timestamps -----
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    // ----- SLA -----
    private LocalDateTime responseDeadline;
    private LocalDateTime resolutionDeadline;
    @Column(nullable = false)
    private boolean responseBreached = false;
    @Column(nullable = false)
    private boolean resolutionBreached = false;

    // ----- resolution -----
    @Column(columnDefinition = "TEXT")
    private String resolutionSummary;

    @Column(columnDefinition = "TEXT")
    private String closureNotes;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<InvestigationNote> notes = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;

    protected Incident() {
    }

    public Incident(String title, String description, Employee reporter, ItService service,
                    Impact impact, Urgency urgency, int affectedUsers) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (reporter == null || impact == null || urgency == null) {
            throw new IllegalArgumentException("Reporter, impact and urgency are required");
        }
        if (affectedUsers < 1) {
            throw new IllegalArgumentException("At least one user must be affected");
        }
        this.title = title.trim();
        this.description = description.trim();
        this.reporter = reporter;
        this.service = service;
        this.impact = impact;
        this.urgency = urgency;
        this.affectedUsers = affectedUsers;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= business actions =================

    public void classify(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        this.category = category;
    }

    public void applyPriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority is required");
        }
        this.priority = priority;
    }

    public void applySlaDeadlines(LocalDateTime responseDeadline, LocalDateTime resolutionDeadline) {
        this.responseDeadline = responseDeadline;
        this.resolutionDeadline = resolutionDeadline;
    }

    public void assignTo(Technician technician) {
        if (technician == null) {
            throw new IllegalArgumentException("Technician is required");
        }
        this.assignedTechnician = technician;
        this.assignedAt = LocalDateTime.now();
    }

    /**
     * Low-level status change. Records the matching timestamp.
     * Call this only from the State pattern classes (com.itsm.state),
     * which check that the transition is allowed.
     */
    public void changeStatus(IncidentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required");
        }
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case ACKNOWLEDGED -> this.acknowledgedAt = now;
            case RESOLVED -> this.resolvedAt = now;
            case CLOSED -> this.closedAt = now;
            default -> { }
        }
        this.status = newStatus;
    }

    public InvestigationNote addNote(User author, String content) {
        InvestigationNote note = new InvestigationNote(this, author, content);
        notes.add(note);
        return note;
    }

    public void recordResolution(String summary) {
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Resolution summary is required");
        }
        this.resolutionSummary = summary.trim();
    }

    public void recordClosureNotes(String notes) {
        this.closureNotes = notes;
    }

    public void giveFeedback(int rating, String comment) {
        if (status != IncidentStatus.RESOLVED && status != IncidentStatus.VERIFIED
                && status != IncidentStatus.CLOSED) {
            throw new IllegalStateException("Feedback can only be given after the incident is resolved");
        }
        this.feedback = new Feedback(rating, comment);
    }

    public void markResponseBreached() {
        this.responseBreached = true;
    }

    public void markResolutionBreached() {
        this.resolutionBreached = true;
    }

    /** Human-friendly ID such as INC0007. */
    public String getReference() {
        return id == null ? "INC-NEW" : String.format("INC%04d", id);
    }

    public boolean isOpen() {
        return status.isOpen();
    }

    // ================= getters =================

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Employee getReporter() { return reporter; }
    public ItService getService() { return service; }
    public Category getCategory() { return category; }
    public Impact getImpact() { return impact; }
    public Urgency getUrgency() { return urgency; }
    public int getAffectedUsers() { return affectedUsers; }
    public Priority getPriority() { return priority; }
    public IncidentStatus getStatus() { return status; }
    public Technician getAssignedTechnician() { return assignedTechnician; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public LocalDateTime getResponseDeadline() { return responseDeadline; }
    public LocalDateTime getResolutionDeadline() { return resolutionDeadline; }
    public boolean isResponseBreached() { return responseBreached; }
    public boolean isResolutionBreached() { return resolutionBreached; }
    public String getResolutionSummary() { return resolutionSummary; }
    public String getClosureNotes() { return closureNotes; }
    public Feedback getFeedback() { return feedback; }

    /** Read-only view; add notes with addNote(). */
    public List<InvestigationNote> getNotes() {
        return Collections.unmodifiableList(notes);
    }
}
