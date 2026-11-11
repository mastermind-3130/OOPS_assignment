package com.itsm.domain.incident;

import com.itsm.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** A note added while investigating an incident. Created only through Incident.addNote(). */
@Entity
@Table(name = "investigation_notes")
public class InvestigationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected InvestigationNote() {
    }

    InvestigationNote(Incident incident, User author, String content) {
        if (author == null) {
            throw new IllegalArgumentException("Author is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Note content is required");
        }
        this.incident = incident;
        this.author = author;
        this.content = content.trim();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
