# I-ITSM — Project Instructions

Intelligent IT Service Management & Incident Resolution System
CS F213 / MAC F212 OOP Project, Semester I 2026-2027

Read this file before writing any code. Paste it into an AI chat as context when asking for help.

---

## 1. What we are building

A web application that manages the full lifecycle of IT incidents:

Report → Classify → Prioritize → Assign → Investigate → Resolve → Verify → Close

### Users
- **Employee**: register/login, report incidents, track status, add information, respond to requests, view resolution, give feedback.
- **Technician**: view assigned incidents, acknowledge, add investigation notes, update status, request more info, resolve, view workload.
- **Admin**: manage users, technicians, IT services, categories, SLA policies and the active assignment strategy; monitor incidents, SLA violations, reports and audit logs.

### Grading focus
- Working MVP = 60 / 120 marks. Finish the MVP before extras.
- OOP design quality is heavily judged: encapsulation, abstraction, inheritance only for real "is-a", polymorphism, interfaces, composition, custom exceptions, suitable collections.
- At least 2 design patterns (we plan 5+).

---

## 2. Rules

- **Free tools and services only.** Any paid plan or service needs approval first.
- **Never commit secrets** (DB password, email app password, Telegram token, API keys). Read them from environment variables, e.g. `${DB_PASSWORD}`.
- **Never push directly to `main`.** Use feature branches and pull requests.
- Use the shared names in section 5 exactly, so everyone's code fits together.
- Every member must understand and be able to explain their code in the demo video.

---

## 3. Tech stack

| Layer | Choice |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot 4.1.x (Maven) |
| Frontend | Thymeleaf + Bootstrap 5, Chart.js (optional HTMX) |
| Database | PostgreSQL (Docker Compose locally), Spring Data JPA |
| Security | Spring Security form login, BCrypt, role-based access |
| API docs | springdoc-openapi (Swagger UI at `/swagger-ui.html`) |
| Testing | JUnit 5, Mockito, Spring MockMvc |
| API manual testing | Swagger UI, Bruno (collections in `/bruno`) |
| CI | GitHub Actions (`mvn test` on every push) |
| Notifications | In-app + Email (Gmail SMTP) and/or Telegram bot |

---

## 4. Project structure

```
itsm/
├── .github/workflows/ci.yml
├── .gitignore
├── INSTRUCTIONS.md
├── README.md
├── docker-compose.yml
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
├── docs/                    # report, UML/ER diagrams, test-cases.md
├── bruno/                   # API request collections
└── src/
    ├── main/java/com/itsm/
    │   ├── ItsmApplication.java
    │   ├── config/          # app config, sample data seeder
    │   ├── security/        # security config, login handling
    │   ├── domain/
    │   │   ├── user/        # User (abstract), Employee, Technician, Admin
    │   │   ├── incident/    # Incident, InvestigationNote, Feedback
    │   │   ├── catalog/     # ItService, Category, SlaPolicy
    │   │   ├── audit/       # AuditLog
    │   │   ├── notification/# Notification (stored in-app message)
    │   │   └── enums/       # IncidentStatus, Priority, Impact, Urgency, Role
    │   ├── state/           # IncidentState + one class per status
    │   ├── priority/        # PriorityCalculator + implementations
    │   ├── assignment/      # AssignmentStrategy + implementations
    │   ├── sla/             # SLA calculation, monitor, escalation
    │   ├── event/           # events + listeners (Observer)
    │   ├── notify/          # NotificationChannel + channels + factory
    │   ├── intelligence/    # duplicate detection, auto-categorization
    │   ├── repository/      # Spring Data JPA repositories
    │   ├── service/         # business logic
    │   ├── web/             # Thymeleaf page controllers
    │   ├── api/             # REST controllers (/api/...)
    │   ├── dto/             # form + API objects
    │   └── exception/       # ItsmException, custom exceptions, global handler
    ├── main/resources/
    │   ├── application.properties
    │   ├── templates/       # fragments/, layout/, auth/, employee/,
    │   │                    # technician/, admin/, incident/, reports/
    │   └── static/          # css/, js/, img/
    └── test/java/com/itsm/  # mirrors main packages
```

---

## 5. Shared domain names (use exactly these)

**Users** — abstract `User` → `Employee`, `Technician`, `Admin` (JPA `SINGLE_TABLE` inheritance)

**Entities** — `Incident`, `ItService`, `Category`, `SlaPolicy`, `InvestigationNote`, `Feedback`, `AuditLog`, `Notification`, `SlaViolation`

**Enums**
- `IncidentStatus` { NEW, CLASSIFIED, ASSIGNED, ACKNOWLEDGED, IN_PROGRESS, PENDING_INFO, RESOLVED, VERIFIED, CLOSED }
- `Priority` { P1, P2, P3, P4 }
- `Impact` { LOW, MEDIUM, HIGH }
- `Urgency` { LOW, MEDIUM, HIGH }
- `Role` { EMPLOYEE, TECHNICIAN, ADMIN }

**Design patterns**
| Pattern | Where |
|---|---|
| State | `IncidentState` — invalid transitions throw `InvalidStateTransitionException` |
| Strategy | `PriorityCalculator`; `AssignmentStrategy` (SkillBased, LoadBased, RoundRobin) |
| Observer | Spring events: `IncidentCreatedEvent`, `IncidentAssignedEvent`, `IncidentStatusChangedEvent`, `SlaWarningEvent`, `SlaViolatedEvent`; listeners for audit, notification, SLA |
| Factory | `NotificationChannelFactory`, incident creation |
| Adapter | `NotificationChannel` → `InAppChannel`, `EmailChannel`, `TelegramChannel` |
| Repository | Spring Data JPA repositories |

**Exceptions** — all custom exceptions extend `ItsmException`
(e.g. `InvalidStateTransitionException`, `UnauthorizedOperationException`, `TechnicianUnavailableException`, `WorkloadExceededException`, `ServiceNotFoundException`, `SlaPolicyNotFoundException`, `DuplicateIncidentException`)

---

## 6. MVP checklist

- [ ] User management: 3 roles, register/login, role-based pages
- [ ] Incidents: create, view, update, assign, resolve, close
- [ ] Lifecycle with blocked invalid transitions
- [ ] Priority from impact × urgency × business criticality → P1–P4
- [ ] 2+ assignment strategies, switchable by admin
- [ ] SLA: deadlines, response/resolution tracking, warnings, violations
- [ ] 2+ notification mechanisms
- [ ] Full audit trail (timestamp, user, action, incident, old/new state, description)
- [ ] Search with 5+ filters; reports (overview, technician workload, service analysis)
- [ ] 2+ intelligent features (duplicate detection, auto-escalation, ...)
- [ ] PostgreSQL persistence for all key data
- [ ] 15+ documented test cases

---

## 7. Timeline (deadline 22 Nov 2026; Nov 15–22 is a busy week)

| Dates | Goal |
|---|---|
| Nov 3–4 | Skeleton in GitHub, domain model agreed, everyone runs it locally |
| Nov 5–8 | Core MVP build on feature branches |
| Nov 9 | Checkpoint: report → priority → assign → SLA → notify → audit works end to end |
| Nov 10–12 | Finish MVP + intelligent features + tests |
| Nov 13 | Feature freeze; report sections and diagrams done |
| Nov 14 | Record demo video together (3 workflows, every member presents) |
| Nov 15–21 | Assemble report, final fixes, submit (buffer before Nov 22) |

---

## 8. Git workflow

1. `git pull` on `main` before starting work.
2. Create a branch: `git checkout -b feature/<short-name>`
3. Commit small, clear changes: `git commit -m "Add SLA deadline calculation"`
4. Push the branch and open a pull request on GitHub.
5. A teammate reviews, then merge into `main`.

---

## 9. Setup progress

- [ ] Step 1: Add INSTRUCTIONS.md to repo
- [ ] Step 2: Generate Spring Boot project into the repo folder
- [ ] Step 3: Create package folders
- [ ] Step 4: .gitignore, application.properties, docker-compose.yml
- [ ] Step 5: Run the app locally against PostgreSQL
- [ ] Step 6: README + CI workflow
- [ ] Step 7: Push and add collaborators
