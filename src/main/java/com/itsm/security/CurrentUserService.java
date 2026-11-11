package com.itsm.security;

import com.itsm.domain.user.User;
import com.itsm.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

/** Finds the logged-in user's entity. Use this in controllers instead of repeating lookups. */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> find(Principal principal) {
        if (principal == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(principal.getName());
    }

    /** Returns the logged-in user as the given type, e.g. require(principal, Technician.class). */
    public <T extends User> T require(Principal principal, Class<T> type) {
        User user = find(principal)
                .orElseThrow(() -> new AccessDeniedException("Not logged in"));
        if (!type.isInstance(user)) {
            throw new AccessDeniedException("This page is not available for your role");
        }
        return type.cast(user);
    }
}
