package com.itsm.priority;

import com.itsm.domain.catalog.ItService;
import com.itsm.domain.enums.BusinessCriticality;
import com.itsm.domain.enums.Impact;
import com.itsm.domain.enums.Priority;
import com.itsm.domain.enums.Urgency;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the priority calculation.
 * Plain Java: no Spring and no database needed.
 */
class WeightedPriorityCalculatorTest {

    private final PriorityCalculator calculator = new WeightedPriorityCalculator();
    private final Employee reporter = new Employee("test", "hash", "Test User", "test@itsm.local", "IT");

    private Incident incident(BusinessCriticality criticality, Impact impact, Urgency urgency, int affectedUsers) {
        ItService service = new ItService("Test service", "For tests", criticality);
        return new Incident("Test incident", "Something is broken", reporter, service,
                impact, urgency, affectedUsers);
    }

    @Test
    void highImpactHighUrgencyOnCriticalServiceIsP1() {
        Incident incident = incident(BusinessCriticality.CRITICAL, Impact.HIGH, Urgency.HIGH, 1);

        assertEquals(Priority.P1, calculator.calculate(incident));
    }

    @Test
    void lowImpactLowUrgencyOnLowServiceIsP4() {
        Incident incident = incident(BusinessCriticality.LOW, Impact.LOW, Urgency.LOW, 1);

        assertEquals(Priority.P4, calculator.calculate(incident));
    }

    @Test
    void manyAffectedUsersRaisesPriorityByOneLevel() {
        Priority fewUsers = calculator.calculate(incident(BusinessCriticality.LOW, Impact.LOW, Urgency.LOW, 1));
        Priority manyUsers = calculator.calculate(incident(BusinessCriticality.LOW, Impact.LOW, Urgency.LOW, 50));

        assertEquals(fewUsers.getLevel() - 1, manyUsers.getLevel());
    }

    @Test
    void p1StaysP1EvenWithManyAffectedUsers() {
        Incident incident = incident(BusinessCriticality.CRITICAL, Impact.HIGH, Urgency.HIGH, 500);

        assertEquals(Priority.P1, calculator.calculate(incident));
    }
}
