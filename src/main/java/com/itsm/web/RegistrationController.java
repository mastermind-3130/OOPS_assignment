package com.itsm.web;

import com.itsm.dto.RegistrationForm;
import com.itsm.exception.ItsmException;
import com.itsm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String form(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.registerEmployee(form.getUsername(), form.getPassword(), form.getFullName(),
                    form.getEmail(), form.getDepartment());
            redirect.addFlashAttribute("success", "Account created. You can log in now.");
            return "redirect:/login";
        } catch (ItsmException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}