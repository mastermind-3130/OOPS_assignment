package com.itsm.web;

import com.itsm.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Map;

@Controller
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/admin/reports")
    public String reports(Model model) {
        Map<String, Long> byStatus = reportService.incidentsByStatus();
        Map<String, Long> byPriority = reportService.incidentsByPriority();
        Map<String, Long> byService = reportService.incidentsByService();
        Map<String, Long> byCategory = reportService.incidentsByCategory();
        Map<String, Long> workload = reportService.technicianWorkload();

        model.addAttribute("byStatus", byStatus);
        model.addAttribute("byPriority", byPriority);
        model.addAttribute("byService", byService);
        model.addAttribute("byCategory", byCategory);
        model.addAttribute("workload", workload);
        model.addAttribute("slaViolations", reportService.slaViolationCount());
        model.addAttribute("avgResolution", Math.round(reportService.averageResolutionMinutes()));

        // chart data (labels and counts as separate lists)
        model.addAttribute("statusLabels", new ArrayList<>(byStatus.keySet()));
        model.addAttribute("statusCounts", new ArrayList<>(byStatus.values()));
        model.addAttribute("serviceLabels", new ArrayList<>(byService.keySet()));
        model.addAttribute("serviceCounts", new ArrayList<>(byService.values()));
        model.addAttribute("workloadLabels", new ArrayList<>(workload.keySet()));
        model.addAttribute("workloadCounts", new ArrayList<>(workload.values()));
        return "admin/reports";
    }
}