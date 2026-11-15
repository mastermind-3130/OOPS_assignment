# Feature 2 – Priority Calculation

## Formula
`score = impact weight × urgency weight × business criticality weight`

Weights already exist in the project enums:
- Impact: LOW=1, MEDIUM=2, HIGH=3
- Urgency: LOW=1, MEDIUM=2, HIGH=3
- Business criticality: LOW=1, MEDIUM=2, HIGH=3, CRITICAL=4
- Missing service: criticality weight 2

## Thresholds used by this implementation
- score >= 24: P1
- score >= 12: P2
- score >= 4: P3
- otherwise: P4

If at least 50 users are affected, priority is raised by one level (P4→P3→P2→P1).

## Integration
Replace the existing `service/IncidentService.java` with the version in this package.
The constructor gains a `PriorityCalculator` dependency. Spring injects the `@Component`
implementation automatically. Priority is calculated before the incident is saved.
