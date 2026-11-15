package com.itsm.service;

import com.itsm.domain.user.Admin;
import com.itsm.domain.user.Employee;
import com.itsm.domain.user.Technician;
import com.itsm.domain.user.User;
import com.itsm.exception.DuplicateUserException;
import com.itsm.exception.ItsmException;
import com.itsm.repository.TechnicianRepository;
import com.itsm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       TechnicianRepository technicianRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.technicianRepository = technicianRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Employee registerEmployee(String username, String rawPassword, String fullName,
                                     String email, String department) {
        checkNewUser(username, rawPassword, email);
        Employee employee = new Employee(username.trim(), passwordEncoder.encode(rawPassword),
                fullName, email.trim(), department);
        return userRepository.save(employee);
    }

    public Technician createTechnician(String username, String rawPassword, String fullName,
                                       String email, int experienceYears, Set<String> skills) {
        checkNewUser(username, rawPassword, email);
        Technician technician = new Technician(username.trim(), passwordEncoder.encode(rawPassword),
                fullName, email.trim(), experienceYears);
        for (String skill : skills) {
            if (skill != null && !skill.isBlank()) {
                technician.addSkill(skill.trim().toUpperCase());
            }
        }
        return userRepository.save(technician);
    }

    public Admin createAdmin(String username, String rawPassword, String fullName, String email) {
        checkNewUser(username, rawPassword, email);
        Admin admin = new Admin(username.trim(), passwordEncoder.encode(rawPassword),
                fullName, email.trim());
        return userRepository.save(admin);
    }

    public void setActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ItsmException("User not found: " + userId));
        if (active) {
            user.activate();
        } else {
            user.deactivate();
        }
        userRepository.save(user);
    }

    public void updateTechnician(Long id, Set<String> skills, int maxActiveIncidents, boolean available) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ItsmException("Technician not found: " + id));
        if (maxActiveIncidents < 1) {
            throw new IllegalArgumentException("Max active incidents must be at least 1");
        }
        // getSkills() is read-only, so copy before removing
        for (String old : new HashSet<>(technician.getSkills())) {
            technician.removeSkill(old);
        }
        for (String skill : skills) {
            if (skill != null && !skill.isBlank()) {
                technician.addSkill(skill.trim().toUpperCase());
            }
        }
        technician.changeMaxActiveIncidents(maxActiveIncidents);
        if (available) {
            technician.markAvailable();
        } else {
            technician.markUnavailable();
        }
        technicianRepository.save(technician);
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Technician> listTechnicians() {
        return technicianRepository.findAll();
    }

    private void checkNewUser(String username, String rawPassword, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (userRepository.existsByUsername(username.trim())) {
            throw new DuplicateUserException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email.trim())) {
            throw new DuplicateUserException("Email already in use: " + email);
        }
    }
}