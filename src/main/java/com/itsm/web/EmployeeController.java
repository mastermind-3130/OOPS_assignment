package com.itsm.web;

import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import com.itsm.security.CurrentUserService;
import com.itsm.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final CurrentUserService currentUserService;
    private final DashboardService dashboardService;

    public EmployeeController(CurrentUserService currentUserService, DashboardService dashboardService) {
        this.currentUserService = currentUserService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        Employee employee = currentUserService.require(principal, Employee.class);
        List<Incident> incidents = dashboardService.incidentsReportedBy(employee);
        long openCount = incidents.stream().filter(Incident::isOpen).count();

        model.addAttribute("incidents", incidents);
        model.addAttribute("openCount", openCount);
        model.addAttribute("resolvedCount", incidents.size() - openCount);
        return "employee/dashboard";
    }
}
