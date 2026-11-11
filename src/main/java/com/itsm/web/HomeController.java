package com.itsm.web;

import com.itsm.security.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final CurrentUserService currentUserService;

    public HomeController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    /** "/" sends logged-in users to their own dashboard. */
    @GetMapping("/")
    public String home(Principal principal) {
        return currentUserService.find(principal)
                .map(user -> "redirect:" + user.getRole().getDashboardPath())
                .orElse("redirect:/login");
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}
