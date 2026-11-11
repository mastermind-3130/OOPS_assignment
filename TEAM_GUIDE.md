# Team Guide: Where to Start

The base of the app is ready. Pick a feature below, create a branch, and build it.

## What already works

- Login with real accounts from the database (passwords hashed with BCrypt)
- Each role lands on its own dashboard: `/employee/dashboard`, `/technician/dashboard`, `/admin/dashboard`
- Role-based access: an employee can't open admin pages, and so on
- All core entities and repositories exist (see `INSTRUCTIONS.md` section 5)
- Sample data loads automatically on first start

### Sample accounts (password for all: `password123`)

| Role | Usernames |
|---|---|
| Admin | `admin` |
| Employee | `emp1`, `emp2` |
| Technician | `tech1` (NETWORK, DATABASE, CLOUD), `tech2` (SOFTWARE, APPLICATION, HARDWARE), `tech3` (SECURITY, AUTHENTICATION, STORAGE) |

Also loaded: 9 categories, 6 IT services, 4 default SLA policies (P1–P4 from the brief), 3 sample incidents in status NEW.

To reset the data: in psql run `DROP DATABASE itsm;` then `CREATE DATABASE itsm;`, and restart the app.

---

## How the code is organised

| Layer | Package | Rule |
|---|---|---|
| Pages | `web` + `templates/` | Controllers stay thin: get input, call a service, pick a template |
| Business logic | `service` | All rules and workflows live here |
| Domain | `domain/...` | Entities with behaviour methods; no public setters |
| Patterns | `state`, `priority`, `assignment`, `sla`, `event`, `notify`, `intelligence` | One folder per pattern/feature |
| Data | `repository` | Spring Data interfaces only |

Useful helpers that already exist:
- `CurrentUserService.require(principal, Technician.class)` gets the logged-in user as the right type.
- Every page automatically has `currentUser` available (for the navbar).
- New pages: copy an existing dashboard template and keep the `head`, `navbar` and `scripts` fragments.
- New custom exceptions extend `ItsmException`.

---

## Features to build

Each item lists where the code goes. Build in roughly this order, because later features depend on earlier ones.

### 1. Incident reporting and lifecycle (start first)
- `service/IncidentService`: create incident, view, update, assign, resolve, close
- `state/`: `IncidentState` interface + one class per status. Each class allows only valid next statuses and throws `InvalidStateTransitionException` otherwise. Only state classes call `incident.changeStatus()`.
- Pages: report form (`/employee/incidents/new`), incident detail page, technician actions (acknowledge, add note, request info, resolve), employee feedback
- Turn on the disabled "Report an incident" button on the employee dashboard

### 2. Priority calculation
- `priority/PriorityCalculator` interface + an implementation using impact × urgency × business criticality (+ affected users)
- Called by `IncidentService` when an incident is created
- Document the formula and thresholds in the report

### 3. Technician assignment
- `assignment/AssignmentStrategy` interface + at least 2 implementations: `SkillBasedAssignment`, `LoadBasedAssignment` (and optionally `RoundRobinAssignment`)
- Skills match `Category.code`. Workload = `IncidentRepository.countByAssignedTechnicianAndStatusIn(...)`
- Admin page to choose the active strategy
- Throw `TechnicianUnavailableException` / `WorkloadExceededException` when nobody fits

### 4. SLA management
- `sla/`: find the policy (`SlaPolicyRepository` — service-specific first, then default), set deadlines on the incident
- `@Scheduled` monitor: warn before deadlines, mark breaches, save `SlaViolation`
- Needs `@EnableScheduling` on a config class
- Intelligent feature: automatic escalation of unacknowledged P1 incidents

### 5. Events, notifications and audit (Observer pattern)
- `event/`: event classes (`IncidentCreatedEvent`, `IncidentAssignedEvent`, ...) published with Spring's `ApplicationEventPublisher`
- Listeners: one saves `AuditLog`, one sends notifications
- `notify/`: `NotificationChannel` interface, `InAppChannel` (saves `Notification`), `EmailChannel` or `TelegramChannel`, `NotificationChannelFactory`
- Show unread notifications in the navbar

### 6. Admin management pages
- Users and technicians, IT services, categories, SLA policies
- Replace the disabled links on the admin dashboard

### 7. Search and reports
- Search page with 5+ filters using `JpaSpecificationExecutor` (already on `IncidentRepository`)
- Reports: technician workload, incidents by service, SLA summary (Chart.js)

### 8. Intelligent features (need 2+)
- Duplicate detection in `intelligence/` (compare new incident text with open incidents)
- Automatic escalation (with SLA, item 4)
- Optional: auto-categorisation, suggested resolutions

### 9. Also required
- Employee self-registration page
- REST API in `api/` + Swagger (`springdoc-openapi`)
- 15+ tests (JUnit 5, Mockito, MockMvc), documented in `docs/test-cases.md`

---

## Workflow for every feature

```
git checkout main
git pull
git checkout -b feature/<name>
# build and test
git add .
git commit -m "Describe the change"
git push -u origin feature/<name>
```

Then open a Pull Request on GitHub, get one teammate to review, and merge.

**Before changing a shared file** (any entity, `pom.xml`, `application.properties`, `SecurityConfig`, `fragments/layout.html`), tell the group first.
