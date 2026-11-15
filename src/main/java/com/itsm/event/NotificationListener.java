package com.itsm.event;

import com.itsm.domain.enums.Role;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.User;
import com.itsm.notify.NotificationChannel;
import com.itsm.notify.NotificationChannelFactory;
import com.itsm.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Observer 2: works out who to tell and sends through every channel for that action. */
@Component
public class NotificationListener {

    private static final Set<String> ADMIN_ACTIONS =
            Set.of("SLA_VIOLATED", "ESCALATED", "ASSIGNMENT_FAILED");

    private final UserRepository userRepository;
    private final NotificationChannelFactory channelFactory;

    public NotificationListener(UserRepository userRepository,
                                NotificationChannelFactory channelFactory) {
        this.userRepository = userRepository;
        this.channelFactory = channelFactory;
    }

    @EventListener
    public void onIncidentEvent(IncidentEvent event) {
        Incident incident = event.incident();
        Map<Long, User> recipients = new LinkedHashMap<>();   // one entry per person

        addIfNotActor(recipients, incident.getReporter(), event.actor());
        addIfNotActor(recipients, incident.getAssignedTechnician(), event.actor());

        if (ADMIN_ACTIONS.contains(event.action())) {
            userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .forEach(admin -> addIfNotActor(recipients, admin, event.actor()));
        }

        for (User recipient : recipients.values()) {
            for (NotificationChannel channel : channelFactory.channelsFor(event.action())) {
                channel.send(recipient, incident.getId(), event.description());
            }
        }
    }

    private void addIfNotActor(Map<Long, User> recipients, User user, String actor) {
        if (user == null) {
            return;
        }
        if (user.getUsername().equals(actor)) {
            return;   // do not notify the person who did it
        }
        recipients.put(user.getId(), user);
    }
}
