package com.itsm.web;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.repository.*;
import com.itsm.sla.SlaService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class AdminSlaController {

    private final SlaViolationRepository slaViolationRepository;
    private final IncidentRepository incidentRepository;
    private final SlaService slaService;

    public AdminSlaController(SlaViolationRepository slaViolationRepository,
                              IncidentRepository incidentRepository,
                              SlaService slaService) {
        this.slaViolationRepository = slaViolationRepository;
        this.incidentRepository = incidentRepository;
        this.slaService = slaService;
    }

    @GetMapping("/admin/sla")
    public String page(Model model) {
        var open = Arrays.stream(IncidentStatus.values())
                .filter(IncidentStatus::isOpen).toList();
        model.addAttribute("violations",
                slaViolationRepository.findAll(Sort.by(Sort.Direction.DESC, "detectedAt")));
        model.addAttribute("atRisk", incidentRepository.findByStatusIn(open).stream()
                .filter(i -> "AT_RISK".equals(slaService.statusOf(i))).toList());
        return "admin/sla";
    }
}
