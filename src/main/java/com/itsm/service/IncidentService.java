package com.itsm.service;

import com.itsm.domain.audit.AuditLog;
import com.itsm.domain.catalog.ItService;
import com.itsm.domain.catalog.Category;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import com.itsm.domain.enums.IncidentStatus;
import com.itsm.dto.IncidentForm;
import com.itsm.exception.IncidentNotFoundException;
import com.itsm.exception.ItsmException;
import com.itsm.exception.ServiceNotFoundException;
import com.itsm.repository.AuditLogRepository;
import com.itsm.repository.IncidentRepository;
import com.itsm.repository.ItServiceRepository;
import com.itsm.repository.CategoryRepository;
import com.itsm.repository.TechnicianRepository;
import com.itsm.state.IncidentLifecycle;
import com.itsm.assignment.AssignmentService;
import com.itsm.exception.TechnicianUnavailableException;
import com.itsm.exception.WorkloadExceededException;
import com.itsm.priority.PriorityCalculator;
import com.itsm.domain.user.Technician;
import com.itsm.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
    public class IncidentService {
        private final IncidentRepository incidentRepository;
        private final ItServiceRepository serviceRepository;
        private final AuditLogRepository auditLogRepository;
        private final TechnicianRepository technicianRepository;
        private final IncidentLifecycle lifecycle;
        private final AssignmentService assignmentService;
        private final CategoryRepository categoryRepository;
        private final PriorityCalculator priorityCalculator;

        public IncidentService(IncidentRepository incidentRepository, ItServiceRepository serviceRepository, AuditLogRepository auditLogRepository, TechnicianRepository technicianRepository, IncidentLifecycle lifecycle, AssignmentService assignmentService, CategoryRepository categoryRepository, PriorityCalculator priorityCalculator) {
            this.incidentRepository = incidentRepository;
            this.serviceRepository = serviceRepository;
            this.auditLogRepository = auditLogRepository;
            this.technicianRepository = technicianRepository;
            this.lifecycle = lifecycle;
            this.assignmentService = assignmentService;
            this.categoryRepository = categoryRepository;
            this.priorityCalculator = priorityCalculator;
        }

        public Incident findById(Long id) {
            return incidentRepository.findById(id).orElseThrow(() -> new IncidentNotFoundException("Incident "+ id+" not found"));
        }
        public Incident create(IncidentForm form,Employee reporter){
            //get the service the employee picked
            ItService service = serviceRepository.findById(form.getServiceId()).orElseThrow(() -> new ServiceNotFoundException("Service "+form.getServiceId()+" not found"));
            //build the incident object
            Incident incident = new Incident(form.getTitle(), form.getDescription(), reporter, service, form.getImpact(), form.getUrgency(), form.getAffectedUsers());
            if (form.getCategoryId() != null) {
                Category category = categoryRepository.findById(form.getCategoryId())
                        .orElseThrow(() -> new ItsmException("Category not found"));
                incident.classify(category);
            }


            incident.applyPriority(priorityCalculator.calculate(incident));
            Incident savedIncident = incidentRepository.save(incident);

                auditLogRepository.save(new AuditLog(reporter.getUsername(), "INCIDENT_CREATED",
                savedIncident.getId(), null, savedIncident.getStatus().name(),
                savedIncident.getReference() + " created: " + savedIncident.getTitle()));

                try {
                    assignmentService.assignCandidate(savedIncident).ifPresent(technician -> {
                        String previous = savedIncident.getStatus().name();
                        savedIncident.assignTo(technician);
                        lifecycle.moveTo(savedIncident, IncidentStatus.ASSIGNED);
                        incidentRepository.save(savedIncident);
                        auditLogRepository.save(new AuditLog("SYSTEM", "AUTO_ASSIGNED",
                                savedIncident.getId(), previous, IncidentStatus.ASSIGNED.name(),
                                savedIncident.getReference() + " automatically assigned to "
                                        + technician.getFullName()));
                    });
                } catch (TechnicianUnavailableException | WorkloadExceededException ignored) {
                    // Keep the newly created incident in NEW when no technician can take it.
                }

                return savedIncident;
        }
        private Incident changeStatus(Incident incident, IncidentStatus next , User actor, String action, String description){
            String previous = incident.getStatus().name();
            //making the move to the next status
            lifecycle.moveTo(incident, next);
            Incident savedIncident = incidentRepository.save(incident);
               auditLogRepository.save(new AuditLog(actor.getUsername(), action,
                savedIncident.getId(), previous, next.name(), description));
                return savedIncident;

        }

        // ---------- admin ----------

        //set the technician, then move to ASSIGNED
        public Incident assign(Long id, Long technicianId, User assignedBy){
            Incident incident = findById(id);
            Technician technician = technicianRepository.findById(technicianId)
                    .orElseThrow(() -> new ItsmException("Technician " + technicianId + " not found"));
            incident.assignTo(technician);
            return changeStatus(incident, IncidentStatus.ASSIGNED, assignedBy,
                    "ASSIGNED", incident.getReference() + " assigned to " + technician.getFullName());
        }

        // ---------- technician ----------

        public Incident acknowledge(Long id, Technician technician){
            Incident incident = findById(id);
            return changeStatus(incident, IncidentStatus.ACKNOWLEDGED, technician,
                    "ACKNOWLEDGED", technician.getFullName() + " acknowledged " + incident.getReference());
        }
        public Incident startWork(Long id, Technician technician){
            Incident incident = findById(id);
            return changeStatus(incident, IncidentStatus.IN_PROGRESS, technician,
                    "WORK_STARTED", technician.getFullName() + " started work on " + incident.getReference());
        }
        public Incident requestInfo(Long id, Technician technician){
            Incident incident = findById(id);
            return changeStatus(incident, IncidentStatus.PENDING_INFO, technician,
                    "INFO_REQUESTED", technician.getFullName() + " requested more information for " + incident.getReference());
        }
        //record the resolution text, then move to RESOLVED
        public Incident resolve(Long id, String summary, Technician technician){
            Incident incident = findById(id);
            incident.recordResolution(summary);
            return changeStatus(incident, IncidentStatus.RESOLVED, technician,
                    "RESOLVED", technician.getFullName() + " resolved " + incident.getReference());
        }

        // ---------- employee (reporter) ----------

        public Incident provideInfo(Long id, Employee employee){
            Incident incident = findById(id);
            return changeStatus(incident, IncidentStatus.IN_PROGRESS, employee,
                    "INFO_PROVIDED", employee.getFullName() + " provided information for " + incident.getReference());
        }
        public Incident confirmResolved(Long id, Employee employee){
            Incident incident = findById(id);
            return changeStatus(incident, IncidentStatus.VERIFIED, employee,
                    "VERIFIED", employee.getFullName() + " confirmed the fix for " + incident.getReference());
        }
        public Incident close(Long id, Employee employee){
            Incident incident = findById(id);
            return changeStatus(incident, IncidentStatus.CLOSED, employee,
                    "CLOSED", employee.getFullName() + " closed " + incident.getReference());
        }
        public Incident giveFeedback(Long id, int rating, String comment){
            Incident incident = findById(id);
            incident.giveFeedback(rating, comment);
            return incidentRepository.save(incident);
        }

        // ---------- anyone involved ----------

        public Incident addNote(Long id, String content, User author){
            Incident incident = findById(id);
            incident.addNote(author, content);
            return incidentRepository.save(incident);
        }

    }