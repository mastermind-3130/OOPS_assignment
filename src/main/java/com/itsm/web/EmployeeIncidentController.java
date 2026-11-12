package com.itsm.web;

import com.itsm.domain.enums.Impact;
import com.itsm.domain.enums.Urgency;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import com.itsm.dto.IncidentForm;
import com.itsm.exception.ItsmException;
import com.itsm.repository.ItServiceRepository;
import com.itsm.security.CurrentUserService;
import com.itsm.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** The "Report an incident" form for employees. */
@Controller
@RequestMapping("/employee/incidents")
public class EmployeeIncidentController {

    private final IncidentService incidentService;
    private final CurrentUserService currentUserService;
    private final ItServiceRepository serviceRepository;

    public EmployeeIncidentController(IncidentService incidentService,
                                      CurrentUserService currentUserService,
                                      ItServiceRepository serviceRepository) {
        this.incidentService = incidentService;
        this.currentUserService = currentUserService;
        this.serviceRepository = serviceRepository;
    }

    /** Shows the empty form. */
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("form", new IncidentForm());
        addDropdownData(model);
        return "employee/report";
    }

    /** Receives the submitted form. */
    @PostMapping
    public String submit(@Valid @ModelAttribute("form") IncidentForm form,
                         BindingResult errors,
                         Principal principal,
                         Model model,
                         RedirectAttributes redirect) {
        // Validation failed (e.g. empty title): show the form again with the messages.
        if (errors.hasErrors()) {
            addDropdownData(model);
            return "employee/report";
        }

        Employee employee = currentUserService.require(principal, Employee.class);
        try {
            Incident incident = incidentService.create(form, employee);
            redirect.addFlashAttribute("success", incident.getReference() + " has been reported.");
            return "redirect:/incidents/" + incident.getId();
        } catch (ItsmException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            addDropdownData(model);
            return "employee/report";
        }
    }

    private void addDropdownData(Model model) {
        model.addAttribute("services", serviceRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("impacts", Impact.values());
        model.addAttribute("urgencies", Urgency.values());
    }
}
