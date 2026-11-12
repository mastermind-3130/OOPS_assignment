package com.itsm.web;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Admin;
import com.itsm.domain.user.Employee;
import com.itsm.domain.user.Technician;
import com.itsm.domain.user.User;
import com.itsm.exception.IncidentNotFoundException;
import com.itsm.exception.ItsmException;
import com.itsm.repository.AuditLogRepository;
import com.itsm.repository.TechnicianRepository;
import com.itsm.security.CurrentUserService;
import com.itsm.service.IncidentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * The incident detail page (shared by all roles) and every action button on it.
 *
 * Each button posts to its own URL, e.g. POST /incidents/5/acknowledge.
 * The handler checks who is asking, calls IncidentService, and goes back to the detail page.
 */
@Controller
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final CurrentUserService currentUserService;
    private final TechnicianRepository technicianRepository;
    private final AuditLogRepository auditLogRepository;

    public IncidentController(IncidentService incidentService,
                              CurrentUserService currentUserService,
                              TechnicianRepository technicianRepository,
                              AuditLogRepository auditLogRepository) {
        this.incidentService = incidentService;
        this.currentUserService = currentUserService;
        this.technicianRepository = technicianRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ===================== detail page =====================

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        User user = currentUser(principal);
        Incident incident = incidentService.findById(id);
        requireCanView(incident, user);

        boolean isAdmin = user instanceof Admin;
        boolean isReporter = sameUser(incident.getReporter(), user);
        boolean isAssignedTech = sameUser(incident.getAssignedTechnician(), user);
        IncidentStatus status = incident.getStatus();

        model.addAttribute("incident", incident);
        model.addAttribute("history", auditLogRepository.findByIncidentIdOrderByTimestampAsc(id));
        model.addAttribute("backUrl", user.getRole().getDashboardPath());

        // Which buttons to show. The page only shows an action if it makes sense
        // for this user and this status.
        model.addAttribute("canAssign", isAdmin
                && (status == IncidentStatus.NEW || status == IncidentStatus.CLASSIFIED));
        model.addAttribute("canAcknowledge", isAssignedTech && status == IncidentStatus.ASSIGNED);
        model.addAttribute("canStart", isAssignedTech && status == IncidentStatus.ACKNOWLEDGED);
        model.addAttribute("canRequestInfo", isAssignedTech && status == IncidentStatus.IN_PROGRESS);
        model.addAttribute("canResolve", isAssignedTech && status == IncidentStatus.IN_PROGRESS);
        model.addAttribute("canProvideInfo", isReporter && status == IncidentStatus.PENDING_INFO);
        model.addAttribute("canConfirm", isReporter && status == IncidentStatus.RESOLVED);
        model.addAttribute("canClose", isReporter && status == IncidentStatus.VERIFIED);
        model.addAttribute("canFeedback", isReporter && !status.isOpen() && incident.getFeedback() == null);
        model.addAttribute("canAddNote", status != IncidentStatus.CLOSED);

        if (isAdmin) {
            model.addAttribute("technicians", technicianRepository.findAll());
        }
        return "incident/detail";
    }

    // ===================== admin action =====================

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id, @RequestParam Long technicianId,
                         Principal principal, RedirectAttributes redirect) {
        Admin admin = currentUserService.require(principal, Admin.class);
        return run(id, redirect, "Incident assigned.",
                () -> incidentService.assign(id, technicianId, admin));
    }

    // ===================== technician actions =====================

    @PostMapping("/{id}/acknowledge")
    public String acknowledge(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Technician technician = requireAssignedTechnician(id, principal);
        return run(id, redirect, "Incident acknowledged.",
                () -> incidentService.acknowledge(id, technician));
    }

    @PostMapping("/{id}/start")
    public String start(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Technician technician = requireAssignedTechnician(id, principal);
        return run(id, redirect, "Work started.",
                () -> incidentService.startWork(id, technician));
    }

    @PostMapping("/{id}/request-info")
    public String requestInfo(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Technician technician = requireAssignedTechnician(id, principal);
        return run(id, redirect, "More information requested from the reporter.",
                () -> incidentService.requestInfo(id, technician));
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id, @RequestParam String summary,
                          Principal principal, RedirectAttributes redirect) {
        Technician technician = requireAssignedTechnician(id, principal);
        return run(id, redirect, "Incident resolved.",
                () -> incidentService.resolve(id, summary, technician));
    }

    // ===================== reporter actions =====================

    @PostMapping("/{id}/provide-info")
    public String provideInfo(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Employee employee = requireReporter(id, principal);
        return run(id, redirect, "Thanks. The technician can continue working.",
                () -> incidentService.provideInfo(id, employee));
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Employee employee = requireReporter(id, principal);
        return run(id, redirect, "Fix confirmed.",
                () -> incidentService.confirmResolved(id, employee));
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Employee employee = requireReporter(id, principal);
        return run(id, redirect, "Incident closed.",
                () -> incidentService.close(id, employee));
    }

    @PostMapping("/{id}/feedback")
    public String feedback(@PathVariable Long id, @RequestParam int rating,
                           @RequestParam(required = false) String comment,
                           Principal principal, RedirectAttributes redirect) {
        requireReporter(id, principal);
        return run(id, redirect, "Thank you for your feedback.",
                () -> incidentService.giveFeedback(id, rating, comment));
    }

    // ===================== notes (anyone who can view) =====================

    @PostMapping("/{id}/notes")
    public String addNote(@PathVariable Long id, @RequestParam String content,
                          Principal principal, RedirectAttributes redirect) {
        User user = currentUser(principal);
        requireCanView(incidentService.findById(id), user);
        return run(id, redirect, "Note added.",
                () -> incidentService.addNote(id, content, user));
    }

    // ===================== helpers =====================

    /**
     * Runs one action. If a business rule is broken (for example an invalid status change),
     * the error message is shown on the detail page instead of a crash page.
     */
    private String run(Long id, RedirectAttributes redirect, String successMessage, Runnable action) {
        try {
            action.run();
            redirect.addFlashAttribute("success", successMessage);
        } catch (ItsmException | IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/incidents/" + id;
    }

    private User currentUser(Principal principal) {
        return currentUserService.find(principal)
                .orElseThrow(() -> new AccessDeniedException("Not logged in"));
    }

    /** Employees may only open their own incidents. Technicians and admins may open any. */
    private void requireCanView(Incident incident, User user) {
        if (user instanceof Employee && !sameUser(incident.getReporter(), user)) {
            throw new AccessDeniedException("You can only view incidents you reported");
        }
    }

    private Technician requireAssignedTechnician(Long id, Principal principal) {
        Technician technician = currentUserService.require(principal, Technician.class);
        if (!sameUser(incidentService.findById(id).getAssignedTechnician(), technician)) {
            throw new AccessDeniedException("This incident is not assigned to you");
        }
        return technician;
    }

    private Employee requireReporter(Long id, Principal principal) {
        Employee employee = currentUserService.require(principal, Employee.class);
        if (!sameUser(incidentService.findById(id).getReporter(), employee)) {
            throw new AccessDeniedException("Only the reporter can do this");
        }
        return employee;
    }

    private boolean sameUser(User a, User b) {
        return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
    }

    /** Unknown incident ID in the URL: go back to the user's dashboard. */
    @ExceptionHandler(IncidentNotFoundException.class)
    public String notFound() {
        return "redirect:/";
    }
}
