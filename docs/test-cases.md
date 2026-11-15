# I-ITSM Test Cases (Feature 9)

**Status note:** `Planned` means not yet executed. Only change to `Passed` after running the automated test or completing the manual steps. Record failures honestly.

| ID | Area | Steps / input | Expected result | Type | Status |
|---|---|---|---|---|---|
| TC01 | Lifecycle | Create a new incident | Starts as NEW | Unit | Planned |
| TC02 | Lifecycle | Move NEW → ASSIGNED | Allowed | Unit | Planned |
| TC03 | Lifecycle | Move NEW → CLOSED | InvalidStateTransitionException | Unit | Planned |
| TC04 | Lifecycle | Move ASSIGNED → ACKNOWLEDGED | Allowed | Unit | Planned |
| TC05 | Lifecycle | Move RESOLVED → VERIFIED | Allowed | Unit | Planned |
| TC06 | Lifecycle | Move VERIFIED → CLOSED | Allowed | Unit | Planned |
| TC07 | Priority | HIGH impact, HIGH urgency, CRITICAL service | P1 | Unit | Planned |
| TC08 | Priority | LOW impact, LOW urgency, LOW service | P4 | Unit | Planned |
| TC09 | Assignment | NETWORK incident with skill-based assignment | Picks suitable available technician | Unit | Planned |
| TC10 | Assignment | Load-based with different incident counts | Picks lowest workload | Unit | Planned |
| TC11 | SLA | P1 policy response deadline | Five minutes after start | Unit | Planned |
| TC12 | Intelligence | Compare near-identical payment incident descriptions | Similarity above 0.30 | Unit | Planned |
| TC13 | Intelligence | Compare unrelated network and printer descriptions | Similarity below 0.10 | Unit | Planned |
| TC14 | Intelligence | `VPN connection keeps dropping` | Suggests NETWORK | Unit | Planned |
| TC15 | Intelligence | Blank category input | No category suggestion | Unit | Planned |
| TC16 | Authentication | Log in with incorrect password | Login denied | Manual | Planned |
| TC17 | Authorization | As employee, open `/admin/dashboard` | Access denied | Manual | Planned |
| TC18 | Authorization | Employee A opens Employee B's incident | Access denied | Manual | Planned |
| TC19 | Validation | Submit an incident without a title | Validation error | Manual | Planned |
| TC20 | Intelligence | Report near-identical open incidents | Possibly related incidents displayed | Manual | Planned |
| TC21 | Intelligence | Resolve incident, then report a similar one | Prior resolution suggestion displayed for technician | Manual | Planned |
| TC22 | Persistence | Create incident and restart app | Incident still exists in database | Manual | Planned |

## Running automated tests

From the repository root on Windows with JDK 21 activated:

```powershell
.\mvnw.cmd test
```

The whole-suite test may require running PostgreSQL; isolate pure unit tests with:

```powershell
.\mvnw.cmd test "-Dtest=TextSimilarityTest,CategorySuggesterTest"
```

## Manual test recording

For each manual test, record tester, date, steps, actual result, and a screenshot or issue link. Never mark a case passed solely because it is implemented.
