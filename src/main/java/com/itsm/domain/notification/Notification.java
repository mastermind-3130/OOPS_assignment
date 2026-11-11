package com.itsm.domain.notification;

import com.itsm.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** A message shown to a user inside the app (the in-app notification channel). */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    private Long incidentId;

    /** Which channel sent it, e.g. IN_APP, EMAIL, TELEGRAM. */
    @Column(nullable = false, length = 20)
    private String channel;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    protected Notification() {
    }

    public Notification(User recipient, Long incidentId, String channel, String message) {
        if (recipient == null || channel == null || message == null || message.isBlank()) {
            throw new IllegalArgumentException("Recipient, channel and message are required");
        }
        this.recipient = recipient;
        this.incidentId = incidentId;
        this.channel = channel;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public void markRead() {
        this.read = true;
    }

    public Long getId() { return id; }
    public User getRecipient() { return recipient; }
    public Long getIncidentId() { return incidentId; }
    public String getChannel() { return channel; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
}
