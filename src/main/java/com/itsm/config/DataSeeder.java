package com.itsm.config;

import com.itsm.domain.audit.AuditLog;
import com.itsm.domain.catalog.Category;
import com.itsm.domain.catalog.ItService;
import com.itsm.domain.catalog.SlaPolicy;
import com.itsm.domain.enums.BusinessCriticality;
import com.itsm.domain.enums.Impact;
import com.itsm.domain.enums.Priority;
import com.itsm.domain.enums.Urgency;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Admin;
import com.itsm.domain.user.Employee;
import com.itsm.domain.user.Technician;
import com.itsm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads sample data the first time the app starts with an empty database.
 * All sample accounts use the password "password123".
 * To reset: drop and recreate the itsm database, then restart the app.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String SAMPLE_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ItServiceRepository serviceRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final IncidentRepository incidentRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      ItServiceRepository serviceRepository,
                      SlaPolicyRepository slaPolicyRepository,
                      IncidentRepository incidentRepository,
                      AuditLogRepository auditLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.serviceRepository = serviceRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.incidentRepository = incidentRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Sample data already present, skipping seeding.");
            return;
        }
        log.info("Seeding sample data...");

        String hash = passwordEncoder.encode(SAMPLE_PASSWORD);

        // ---------- users ----------
        Admin admin = new Admin("admin", hash, "Priya Sharma", "admin@itsm.local");
        Employee emp1 = new Employee("emp1", hash, "Rahul Verma", "rahul@itsm.local", "Finance");
        Employee emp2 = new Employee("emp2", hash, "Sneha Iyer", "sneha@itsm.local", "Human Resources");

        Technician tech1 = new Technician("tech1", hash, "Arjun Reddy", "arjun@itsm.local", 6);
        tech1.addSkill("NETWORK");
        tech1.addSkill("DATABASE");
        tech1.addSkill("CLOUD");

        Technician tech2 = new Technician("tech2", hash, "Meera Nair", "meera@itsm.local", 3);
        tech2.addSkill("SOFTWARE");
        tech2.addSkill("APPLICATION");
        tech2.addSkill("HARDWARE");

        Technician tech3 = new Technician("tech3", hash, "Karthik Rao", "karthik@itsm.local", 8);
        tech3.addSkill("SECURITY");
        tech3.addSkill("AUTHENTICATION");
        tech3.addSkill("STORAGE");

        userRepository.save(admin);
        userRepository.save(emp1);
        userRepository.save(emp2);
        userRepository.save(tech1);
        userRepository.save(tech2);
        userRepository.save(tech3);

        // ---------- categories ----------
        String[][] categories = {
                {"HARDWARE", "Hardware", "Laptops, desktops, printers and peripherals"},
                {"SOFTWARE", "Software", "Installed applications and licences"},
                {"NETWORK", "Network", "Connectivity, Wi-Fi, VPN, LAN"},
                {"DATABASE", "Database", "Database servers and data access"},
                {"CLOUD", "Cloud", "Cloud platforms and hosted services"},
                {"SECURITY", "Security", "Malware, phishing, suspicious activity"},
                {"AUTHENTICATION", "Authentication", "Login, passwords, SSO, MFA"},
                {"APPLICATION", "Application", "Business applications and portals"},
                {"STORAGE", "Storage", "File shares, backups, disk space"}
        };
        Category application = null;
        Category authentication = null;
        Category hardware = null;
        for (String[] c : categories) {
            Category saved = categoryRepository.save(new Category(c[0], c[1], c[2]));
            switch (saved.getCode()) {
                case "APPLICATION" -> application = saved;
                case "AUTHENTICATION" -> authentication = saved;
                case "HARDWARE" -> hardware = saved;
                default -> { }
            }
        }

        // ---------- IT services ----------
        ItService payment = serviceRepository.save(new ItService("Payment Gateway",
                "Processes customer and vendor payments", BusinessCriticality.CRITICAL));
        serviceRepository.save(new ItService("Customer Database",
                "Main customer records database", BusinessCriticality.CRITICAL));
        serviceRepository.save(new ItService("Email",
                "Company email and calendar", BusinessCriticality.HIGH));
        ItService vpn = serviceRepository.save(new ItService("VPN",
                "Remote access to the internal network", BusinessCriticality.HIGH));
        serviceRepository.save(new ItService("HR Portal",
                "Leave, payroll and employee records", BusinessCriticality.MEDIUM));
        ItService laptops = serviceRepository.save(new ItService("Employee Laptops",
                "Company-issued laptops and desktops", BusinessCriticality.LOW));

        // ---------- default SLA policies (from the project brief) ----------
        slaPolicyRepository.save(new SlaPolicy(Priority.P1, 5, 30, null));
        slaPolicyRepository.save(new SlaPolicy(Priority.P2, 15, 120, null));
        slaPolicyRepository.save(new SlaPolicy(Priority.P3, 60, 480, null));
        slaPolicyRepository.save(new SlaPolicy(Priority.P4, 240, 1440, null));

        // ---------- sample incidents (status NEW; features will move them along) ----------
        Incident i1 = new Incident("Payment application unavailable",
                "Users cannot complete transactions. The payment page shows a server error.",
                emp1, payment, Impact.HIGH, Urgency.HIGH, 120);
        i1.classify(application);

        Incident i2 = new Incident("Cannot connect to VPN",
                "VPN client says authentication failed since this morning.",
                emp2, vpn, Impact.MEDIUM, Urgency.MEDIUM, 5);
        i2.classify(authentication);

        Incident i3 = new Incident("Laptop running very slowly",
                "Laptop takes several minutes to open any application.",
                emp1, laptops, Impact.LOW, Urgency.LOW, 1);
        i3.classify(hardware);

        for (Incident incident : new Incident[]{i1, i2, i3}) {
            Incident saved = incidentRepository.save(incident);
            auditLogRepository.save(new AuditLog(saved.getReporter().getUsername(), "INCIDENT_CREATED",
                    saved.getId(), null, saved.getStatus().name(),
                    saved.getReference() + " created: " + saved.getTitle()));
        }

        log.info("Sample data ready. Log in as admin, emp1, emp2, tech1, tech2 or tech3 with password '{}'.",
                SAMPLE_PASSWORD);
    }
}
