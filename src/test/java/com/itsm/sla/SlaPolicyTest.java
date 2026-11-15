package com.itsm.sla;

import com.itsm.domain.catalog.SlaPolicy;
import com.itsm.domain.enums.Priority;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SlaPolicyTest {
    @Test
    void responseDeadlineAddsMinutes() {
        LocalDateTime t = LocalDateTime.now();
        assertEquals(t.plusMinutes(5),
                new SlaPolicy(Priority.P1, 5, 30, null).responseDeadlineFrom(t));
    }

    @Test
    void resolutionDeadlineAddsMinutes() {
        LocalDateTime t = LocalDateTime.now();
        assertEquals(t.plusMinutes(30),
                new SlaPolicy(Priority.P1, 5, 30, null).resolutionDeadlineFrom(t));
    }

    @Test
    void resolutionShorterThanResponseIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new SlaPolicy(Priority.P1, 30, 5, null));
    }
}
