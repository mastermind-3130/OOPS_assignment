package com.itsm.service;

import com.itsm.domain.catalog.ItService;
import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import com.itsm.domain.user.Technician;
import com.itsm.domain.user.User;
import com.itsm.dto.IncidentForm;
import com.itsm.event.IncidentEvent;
import com.itsm.exception.IncidentNotFoundException;
import com.itsm.exception.ItsmException;
import com.itsm.exception.ServiceNotFoundException;
import com.itsm.repository.IncidentRepository;
import com.itsm.repository.ItServiceRepository;
import com.itsm.repository.TechnicianRepository;
import com.itsm.state.IncidentLifecycle;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final ItServiceRepository serviceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TechnicianRepository technicianRepository;
    private final IncidentLifecycle lifecycle;

    public IncidentService(IncidentRepository incidentRepository,
                           ItServiceRepository serviceRepository,
                           ApplicationEventPublisher eventPublisher,
                           TechnicianRepository technicianRepository,
                           IncidentLifecycle lifecycle) {
        this.incidentRepository = incidentRepository;
        this.serviceRepository = serviceRepository;
        this.eventPublisher = eventPublisher;
        this.technicianRepository = technicianRepository;
        this.lifecycle = lifecycle;
    }

    public Incident findById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident " + id + " not found"));
    }

    public Incident create(IncidentForm form, Employee reporter) {
        // get the service the employee picked
        ItService service = serviceRepository.findById(form.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException("Service " + form.getServiceId() + " not found"));
        // build the incident object
        Incident incident = new Incident(form.getTitle(), form.getDescription(), reporter, service,
                form.getImpact(), form.getUrgency(), form.getAffectedUsers());

        // [F2] priority and [F4] SLA deadlines go here, before the save (see guide section 5)

        Incident savedIncident = incidentRepository.save(incident);

        // announce it; the audit listener writes the audit trail
        eventPublisher.publishEvent(new IncidentEvent(savedIncident, "INCIDENT_CREATED",
                reporter.getUsername(), null, savedIncident.getStatus().name(),
                savedIncident.getReference() + " created: " + savedIncident.getTitle()));

        return savedIncident;
    }

    private Incident changeStatus(Incident incident, IncidentStatus next, User actor,
                                  String action, String description) {
        String previous = incident.getStatus().name();
        // making the move to the next status
        lifecycle.moveTo(incident, next);
        Incident savedIncident = incidentRepository.save(incident);

        eventPublisher.publishEvent(new IncidentEvent(savedIncident, action,
                actor.getUsername(), previous, next.name(), description));
        return savedIncident;
    }

    // ---------- admin ----------

    // set the technician, then move to ASSIGNED
    public Incident assign(Long id, Long technicianId, User assignedBy) {
        Incident incident = findById(id);
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ItsmException("Technician " + technicianId + " not found"));
        incident.assignTo(technician);
        return changeStatus(incident, IncidentStatus.ASSIGNED, assignedBy,
                "ASSIGNED", incident.getReference() + " assigned to " + technician.getFullName());
    }

    // ---------- technician ----------

    public Incident acknowledge(Long id, Technician technician) {
        Incident incident = findById(id);
        return changeStatus(incident, IncidentStatus.ACKNOWLEDGED, technician,
                "ACKNOWLEDGED", technician.getFullName() + " acknowledged " + incident.getReference());
    }

    public Incident startWork(Long id, Technician technician) {
        Incident incident = findById(id);
        return changeStatus(incident, IncidentStatus.IN_PROGRESS, technician,
                "WORK_STARTED", technician.getFullName() + " started work on " + incident.getReference());
    }

    public Incident requestInfo(Long id, Technician technician) {
        Incident incident = findById(id);
        return changeStatus(incident, IncidentStatus.PENDING_INFO, technician,
                "INFO_REQUESTED", technician.getFullName() + " requested more information for " + incident.getReference());
    }

    // record the resolution text, then move to RESOLVED
    public Incident resolve(Long id, String summary, Technician technician) {
        Incident incident = findById(id);
        incident.recordResolution(summary);
        return changeStatus(incident, IncidentStatus.RESOLVED, technician,
                "RESOLVED", technician.getFullName() + " resolved " + incident.getReference());
    }

    // ---------- employee (reporter) ----------

    public Incident provideInfo(Long id, Employee employee) {
        Incident incident = findById(id);
        return changeStatus(incident, IncidentStatus.IN_PROGRESS, employee,
                "INFO_PROVIDED", employee.getFullName() + " provided information for " + incident.getReference());
    }

    public Incident confirmResolved(Long id, Employee employee) {
        Incident incident = findById(id);
        return changeStatus(incident, IncidentStatus.VERIFIED, employee,
                "VERIFIED", employee.getFullName() + " confirmed the fix for " + incident.getReference());
    }

    public Incident close(Long id, Employee employee) {
        Incident incident = findById(id);
        return changeStatus(incident, IncidentStatus.CLOSED, employee,
                "CLOSED", employee.getFullName() + " closed " + incident.getReference());
    }

    public Incident giveFeedback(Long id, int rating, String comment) {
        Incident incident = findById(id);
        incident.giveFeedback(rating, comment);
        return incidentRepository.save(incident);
    }

    // ---------- anyone involved ----------

    public Incident addNote(Long id, String content, User author) {
        Incident incident = findById(id);
        incident.addNote(author, content);
        return incidentRepository.save(incident);
    }
}
