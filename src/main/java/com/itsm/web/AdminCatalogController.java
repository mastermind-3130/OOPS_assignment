package com.itsm.web;

import com.itsm.domain.enums.BusinessCriticality;
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
public class AdminCatalogController {

    private final CatalogService catalogService;

    public AdminCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // ---------- IT services ----------

    @GetMapping("/admin/services")
    public String services(Model model) {
        model.addAttribute("services", catalogService.listServices());
        model.addAttribute("criticalities", BusinessCriticality.values());
        return "admin/services";
    }

    @PostMapping("/admin/services")
    public String createService(@RequestParam String name,
                                @RequestParam(defaultValue = "") String description,
                                @RequestParam BusinessCriticality criticality,
                                RedirectAttributes redirect) {
        try {
            catalogService.createService(name, description, criticality);
            redirect.addFlashAttribute("success", "Service added.");
        } catch (ItsmException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/admin/services/{id}")
    public String updateService(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam(defaultValue = "") String description,
                                @RequestParam BusinessCriticality criticality,
                                @RequestParam(defaultValue = "false") boolean active,
                                RedirectAttributes redirect) {
        try {
            catalogService.updateService(id, name, description, criticality, active);
            redirect.addFlashAttribute("success", "Service updated.");
        } catch (ItsmException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/services";
    }

    // ---------- Categories ----------

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        model.addAttribute("categories", catalogService.listCategories());
        return "admin/categories";
    }

    @PostMapping("/admin/categories")
    public String createCategory(@RequestParam String code,
                                 @RequestParam String name,
                                 @RequestParam(defaultValue = "") String description,
                                 RedirectAttributes redirect) {
        try {
            catalogService.createCategory(code, name, description);
            redirect.addFlashAttribute("success", "Category added.");
        } catch (ItsmException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(defaultValue = "") String description,
                                 @RequestParam(defaultValue = "false") boolean active,
                                 RedirectAttributes redirect) {
        try {
            catalogService.updateCategory(id, name, description, active);
            redirect.addFlashAttribute("success", "Category updated.");
        } catch (ItsmException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}