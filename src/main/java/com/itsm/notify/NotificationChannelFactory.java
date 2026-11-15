package com.itsm.notify;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Factory: picks which channels to use. Spring injects every NotificationChannel bean. */
@Component
public class NotificationChannelFactory {

    private static final Set<String> IMPORTANT_ACTIONS = Set.of(
            "ASSIGNED", "AUTO_ASSIGNED", "RESOLVED", "SLA_VIOLATED", "ESCALATED", "CLOSED");

    private final List<NotificationChannel> channels;

    public NotificationChannelFactory(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    public NotificationChannel get(String name) {
        return channels.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown channel: " + name));
    }

    public List<NotificationChannel> channelsFor(String action) {
        if (IMPORTANT_ACTIONS.contains(action)) {
            return channels;
        }
        return List.of(get("IN_APP"));
    }
}
