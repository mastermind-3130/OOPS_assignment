package com.itsm.notify;

import com.itsm.domain.notification.Notification;
import com.itsm.domain.user.User;
import com.itsm.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Simulated email: logs the message and stores a Notification so it can be shown in the demo. */
@Component
public class EmailChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailChannel.class);

    private final NotificationRepository notificationRepository;

    public EmailChannel(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public String getName() {
        return "EMAIL";
    }

    @Override
    public void send(User recipient, Long incidentId, String message) {
        log.info("EMAIL to " + recipient.getEmail() + ": " + message);
        notificationRepository.save(new Notification(recipient, incidentId, "EMAIL", message));
    }
}
