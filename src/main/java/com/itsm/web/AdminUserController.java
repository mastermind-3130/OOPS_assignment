package com.itsm.web;

import com.itsm.domain.enums.Role;
import com.itsm.exception.ItsmException;
import com.itsm.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Controller
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userService.listUsers());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/admin/users")
    public String create(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam Role role,
                         @RequestParam(defaultValue = "") String department,
                         @RequestParam(defaultValue = "0") int experienceYears,
                         @RequestParam(defaultValue = "") String skills,
                         RedirectAttributes redirect) {
        try {
            switch (role) {
                case EMPLOYEE -> userService.registerEmployee(username, password, fullName, email, department);
                case TECHNICIAN -> userService.createTechnician(username, password, fullName, email,
                        experienceYears, parseSkills(skills));
                case ADMIN -> userService.createAdmin(username, password, fullName, email);
            }
            redirect.addFlashAttribute("success", "User '" + username + "' created.");
        } catch (ItsmException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam boolean active, RedirectAttributes redirect) {
        try {
            userService.setActive(id, active);
            redirect.addFlashAttribute("success", active ? "User activated." : "User deactivated.");
        } catch (ItsmException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/technicians")
    public String technicians(Model model) {
        model.addAttribute("technicians", userService.listTechnicians());
        return "admin/technicians";
    }

    @PostMapping("/admin/technicians/{id}")
    public String updateTechnician(@PathVariable Long id,
                                   @RequestParam(defaultValue = "") String skills,
                                   @RequestParam int maxActiveIncidents,
                                   @RequestParam(defaultValue = "false") boolean available,
                                   RedirectAttributes redirect) {
        try {
            userService.updateTechnician(id, parseSkills(skills), maxActiveIncidents, available);
            redirect.addFlashAttribute("success", "Technician updated.");
        } catch (ItsmException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/technicians";
    }

    private Set<String> parseSkills(String text) {
        Set<String> result = new LinkedHashSet<>();
        Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .forEach(result::add);
        return result;
    }
}