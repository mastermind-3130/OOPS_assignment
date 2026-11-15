package com.itsm.web;

import com.itsm.domain.notification.Notification;
import com.itsm.domain.user.User;
import com.itsm.repository.NotificationRepository;
import com.itsm.security.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationController(NotificationRepository notificationRepository,
                                  CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/notifications")
    @Transactional
    public String list(Principal principal, Model model) {
        User user = currentUserService.require(principal, User.class);
        List<Notification> notifications =
                notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        model.addAttribute("notifications", notifications);

        // opening the page marks everything as read
        notifications.forEach(Notification::markRead);
        notificationRepository.saveAll(notifications);
        return "notifications";
    }
}
