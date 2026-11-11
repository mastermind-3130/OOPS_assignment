package com.itsm.service;

import com.itsm.domain.audit.AuditLog;
import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Priority;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import com.itsm.domain.user.Technician;
import com.itsm.dto.AdminOverview;
import com.itsm.repository.AuditLogRepository;
import com.itsm.repository.IncidentRepository;
import com.itsm.repository.SlaViolationRepository;
import com.itsm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Read-only data for the three dashboards. */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Set<IncidentStatus> OPEN_STATUSES = EnumSet.noneOf(IncidentStatus.class);
    private static final Set<IncidentStatus> RESOLVED_STATUSES =
            EnumSet.of(IncidentStatus.RESOLVED, IncidentStatus.VERIFIED, IncidentStatus.CLOSED);

    static {
        Arrays.stream(IncidentStatus.values())
                .filter(IncidentStatus::isOpen)
                .forEach(OPEN_STATUSES::add);
    }

    private final IncidentRepository incidentRepository;
    private final SlaViolationRepository slaViolationRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public DashboardService(IncidentRepository incidentRepository,
                            SlaViolationRepository slaViolationRepository,
                            AuditLogRepository auditLogRepository,
                            UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.slaViolationRepository = slaViolationRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public List<Incident> incidentsReportedBy(Employee employee) {
        return incidentRepository.findByReporterOrderByCreatedAtDesc(employee);
    }

    public List<Incident> incidentsAssignedTo(Technician technician) {
        return incidentRepository.findByAssignedTechnicianOrderByCreatedAtDesc(technician);
    }

    public long activeIncidentCount(Technician technician) {
        return incidentRepository.countByAssignedTechnicianAndStatusIn(technician, OPEN_STATUSES);
    }

    public AdminOverview adminOverview() {
        return new AdminOverview(
                incidentRepository.count(),
                incidentRepository.countByStatusIn(OPEN_STATUSES),
                incidentRepository.countByPriorityAndStatusIn(Priority.P1, OPEN_STATUSES),
                incidentRepository.countByStatusIn(RESOLVED_STATUSES),
                slaViolationRepository.count(),
                userRepository.count());
    }

    public List<AuditLog> recentActivity() {
        return auditLogRepository.findTop10ByOrderByTimestampDesc();
    }
}
