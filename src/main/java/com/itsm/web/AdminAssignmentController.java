package com.itsm.web;

import com.itsm.assignment.AssignmentService;
import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.user.Technician;
import com.itsm.repository.IncidentRepository;
import com.itsm.repository.TechnicianRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/assignment")
public class AdminAssignmentController {
    private final AssignmentService assignmentService;
    private final TechnicianRepository technicianRepository;
    private final IncidentRepository incidentRepository;

    public AdminAssignmentController(AssignmentService assignmentService,
                                     TechnicianRepository technicianRepository,
                                     IncidentRepository incidentRepository) {
        this.assignmentService = assignmentService;
        this.technicianRepository = technicianRepository;
        this.incidentRepository = incidentRepository;
    }

    @GetMapping
    public String page(Model model) {
        var openStatuses = Arrays.stream(IncidentStatus.values())
                .filter(IncidentStatus::isOpen).toList();

        Map<Long, Long> activeCounts = technicianRepository.findAll().stream()
                .collect(Collectors.toMap(Technician::getId,
                        t -> incidentRepository.countByAssignedTechnicianAndStatusIn(t, openStatuses)));

        model.addAttribute("strategies", assignmentService.getStrategies());
        model.addAttribute("activeStrategy", assignmentService.getActiveStrategyName());
        model.addAttribute("technicians", technicianRepository.findAll());
        model.addAttribute("activeCounts", activeCounts);
        return "admin/assignment";
    }

    @PostMapping
    public String changeStrategy(@RequestParam String strategy, RedirectAttributes redirect) {
        try {
            assignmentService.setActiveStrategy(strategy);
            redirect.addFlashAttribute("success", "Assignment strategy updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/assignment";
    }
}
