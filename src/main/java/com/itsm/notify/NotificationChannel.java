package com.itsm.notify;

import com.itsm.domain.user.User;

/** One way of delivering a notification. Add a new class to add a new channel. */
public interface NotificationChannel {

    String getName();   // "IN_APP", "EMAIL"

    void send(User recipient, Long incidentId, String message);
}
