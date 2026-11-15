package com.itsm.web;

import com.itsm.exception.ItsmException;
import com.itsm.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminSlaPolicyController {

    private final CatalogService catalogService;

    public AdminSlaPolicyController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/admin/sla-policies")
    public String policies(Model model) {
        model.addAttribute("policies", catalogService.listSlaPolicies());
        return "admin/sla-policies";
    }

    @PostMapping("/admin/sla-policies/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam int responseMinutes,
                         @RequestParam int resolutionMinutes,
                         RedirectAttributes redirect) {
        try {
            catalogService.updateSlaPolicy(id, responseMinutes, resolutionMinutes);
            redirect.addFlashAttribute("success", "SLA policy updated.");
        } catch (ItsmException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sla-policies";
    }
}