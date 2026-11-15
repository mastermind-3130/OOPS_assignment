package com.itsm.notify;

import com.itsm.domain.notification.Notification;
import com.itsm.domain.user.User;
import com.itsm.repository.NotificationRepository;
import org.springframework.stereotype.Component;

@Component
public class InAppChannel implements NotificationChannel {

    private final NotificationRepository notificationRepository;

    public InAppChannel(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public String getName() {
        return "IN_APP";
    }

    @Override
    public void send(User recipient, Long incidentId, String message) {
        notificationRepository.save(new Notification(recipient, incidentId, "IN_APP", message));
    }
}
