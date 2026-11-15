package com.itsm.notify;

import com.itsm.domain.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationChannelFactoryTest {

    private static NotificationChannel fake(String name) {
        return new NotificationChannel() {
            @Override public String getName() { return name; }
            @Override public void send(User recipient, Long incidentId, String message) { }
        };
    }

    private final NotificationChannelFactory factory =
            new NotificationChannelFactory(List.of(fake("IN_APP"), fake("EMAIL")));

    @Test
    void importantActionUsesBothChannels() {
        assertEquals(2, factory.channelsFor("SLA_VIOLATED").size());
        assertEquals(2, factory.channelsFor("ASSIGNED").size());
    }

    @Test
    void ordinaryActionUsesInAppOnly() {
        List<NotificationChannel> channels = factory.channelsFor("WORK_STARTED");
        assertEquals(1, channels.size());
        assertEquals("IN_APP", channels.get(0).getName());
    }

    @Test
    void unknownChannelThrows() {
        assertThrows(IllegalArgumentException.class, () -> factory.get("SMS"));
    }
}
