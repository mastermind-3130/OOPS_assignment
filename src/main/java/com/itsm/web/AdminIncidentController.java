package com.itsm.web;

import com.itsm.repository.IncidentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Admin list of all incidents.
 * The search feature can later add filters to this page.
 */
@Controller
@RequestMapping("/admin/incidents")
public class AdminIncidentController {

    private final IncidentRepository incidentRepository;

    public AdminIncidentController(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("incidents",
                incidentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin/incidents";
    }
}
