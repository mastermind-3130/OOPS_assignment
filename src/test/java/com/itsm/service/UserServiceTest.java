package com.itsm.service;

import com.itsm.exception.DuplicateUserException;
import com.itsm.repository.TechnicianRepository;
import com.itsm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TechnicianRepository technicianRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    void duplicateUsernameThrows() {
        when(userRepository.existsByUsername("emp1")).thenReturn(true);
        UserService service = new UserService(userRepository, technicianRepository, passwordEncoder);

        assertThrows(DuplicateUserException.class,
                () -> service.registerEmployee("emp1", "password123", "Test User", "t@x.com", "IT"));
    }

    @Test
    void shortPasswordThrows() {
        UserService service = new UserService(userRepository, technicianRepository, passwordEncoder);

        assertThrows(IllegalArgumentException.class,
                () -> service.registerEmployee("newuser", "short", "Test User", "t@x.com", "IT"));
    }
}