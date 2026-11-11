package com.itsm.security;

import com.itsm.domain.user.User;
import com.itsm.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tells Spring Security how to load a user from our database at login.
 * Acts as an adapter between our User entity and Spring Security's UserDetails.
 */
@Service
public class ItsmUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ItsmUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())      // adds the "ROLE_" prefix
                .disabled(!user.isActive())
                .build();
    }
}
