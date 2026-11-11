package com.itsm.web;

import com.itsm.domain.user.Technician;
import com.itsm.security.CurrentUserService;
import com.itsm.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/technician")
public class TechnicianController {

    private final CurrentUserService currentUserService;
    private final DashboardService dashboardService;

    public TechnicianController(CurrentUserService currentUserService, DashboardService dashboardService) {
        this.currentUserService = currentUserService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        Technician technician = currentUserService.require(principal, Technician.class);

        model.addAttribute("technician", technician);
        model.addAttribute("incidents", dashboardService.incidentsAssignedTo(technician));
        model.addAttribute("activeCount", dashboardService.activeIncidentCount(technician));
        return "technician/dashboard";
    }
}
