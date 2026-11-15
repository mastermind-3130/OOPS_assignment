package com.itsm.web;

import com.itsm.domain.user.User;
import com.itsm.repository.NotificationRepository;
import com.itsm.security.CurrentUserService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Makes "currentUser" and "unreadCount" available in every Thymeleaf page (used by the navbar). */
@ControllerAdvice
public class CurrentUserAdvice {

    private final CurrentUserService currentUserService;
    private final NotificationRepository notificationRepository;

    public CurrentUserAdvice(CurrentUserService currentUserService,
                             NotificationRepository notificationRepository) {
        this.currentUserService = currentUserService;
        this.notificationRepository = notificationRepository;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        return currentUserService.find(principal).orElse(null);
    }

    @ModelAttribute("unreadCount")
    public long unreadCount(Principal principal) {
        return currentUserService.find(principal)
                .map(notificationRepository::countByRecipientAndReadFalse)
                .orElse(0L);
    }
}
