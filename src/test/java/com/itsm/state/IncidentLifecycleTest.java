package com.itsm.state;

import com.itsm.domain.enums.Impact;
import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Urgency;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import com.itsm.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the incident lifecycle rules.
 * Plain Java: no Spring and no database needed.
 */
class IncidentLifecycleTest {

    private final IncidentLifecycle lifecycle = new IncidentLifecycle();

    /** Makes a brand-new incident (status NEW) to test with. */
    private Incident newIncident() {
        Employee reporter = new Employee("test", "hash", "Test User", "test@itsm.local", "IT");
        return new Incident("Test incident", "Something is broken", reporter, null,
                Impact.LOW, Urgency.LOW, 1);
    }

    @Test
    void newIncidentStartsWithStatusNew() {
        assertEquals(IncidentStatus.NEW, newIncident().getStatus());
    }

    @Test
    void newIncidentCanBeAssigned() {
        Incident incident = newIncident();

        lifecycle.moveTo(incident, IncidentStatus.ASSIGNED);

        assertEquals(IncidentStatus.ASSIGNED, incident.getStatus());
    }

    @Test
    void newIncidentCannotBeClosedDirectly() {
        Incident incident = newIncident();

        assertThrows(InvalidStateTransitionException.class,
                () -> lifecycle.moveTo(incident, IncidentStatus.CLOSED));

        // the status must be unchanged after a rejected move
        assertEquals(IncidentStatus.NEW, incident.getStatus());
    }

    @Test
    void newIncidentCannotBeResolvedWithoutBeingWorkedOn() {
        Incident incident = newIncident();

        assertThrows(InvalidStateTransitionException.class,
                () -> lifecycle.moveTo(incident, IncidentStatus.RESOLVED));
    }

    @Test
    void incidentCanGoThroughTheFullLifecycle() {
        Incident incident = newIncident();

        lifecycle.moveTo(incident, IncidentStatus.ASSIGNED);
        lifecycle.moveTo(incident, IncidentStatus.ACKNOWLEDGED);
        lifecycle.moveTo(incident, IncidentStatus.IN_PROGRESS);
        lifecycle.moveTo(incident, IncidentStatus.RESOLVED);
        lifecycle.moveTo(incident, IncidentStatus.VERIFIED);
        lifecycle.moveTo(incident, IncidentStatus.CLOSED);

        assertEquals(IncidentStatus.CLOSED, incident.getStatus());
        assertNotNull(incident.getAcknowledgedAt());
        assertNotNull(incident.getResolvedAt());
        assertNotNull(incident.getClosedAt());
    }

    @Test
    void closedIncidentCannotMoveAnywhere() {
        for (IncidentStatus next : IncidentStatus.values()) {
            assertFalse(lifecycle.canMove(IncidentStatus.CLOSED, next),
                    "CLOSED should not be able to move to " + next);
        }
    }

    @Test
    void resolvedIncidentCanBeReopened() {
        assertTrue(lifecycle.canMove(IncidentStatus.RESOLVED, IncidentStatus.IN_PROGRESS));
    }

    @Test
    void pendingInfoGoesBackToInProgress() {
        assertTrue(lifecycle.canMove(IncidentStatus.PENDING_INFO, IncidentStatus.IN_PROGRESS));
        assertFalse(lifecycle.canMove(IncidentStatus.PENDING_INFO, IncidentStatus.RESOLVED));
    }
}
