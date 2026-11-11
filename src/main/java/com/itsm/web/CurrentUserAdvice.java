package com.itsm.web;

import com.itsm.domain.user.User;
import com.itsm.security.CurrentUserService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Makes "currentUser" available in every Thymeleaf page (used by the navbar). */
@ControllerAdvice
public class CurrentUserAdvice {

    private final CurrentUserService currentUserService;

    public CurrentUserAdvice(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        return currentUserService.find(principal).orElse(null);
    }
}
