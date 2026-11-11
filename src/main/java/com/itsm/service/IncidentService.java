package com.itsm.service;

import com.itsm.domain.audit.AuditLog;
import com.itsm.domain.catalog.ItService;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import com.itsm.dto.IncidentForm;
import com.itsm.exception.IncidentNotFoundException;
import com.itsm.exception.ServiceNotFoundException;
import com.itsm.repository.AuditLogRepository;
import com.itsm.repository.IncidentRepository;
import com.itsm.repository.ItServiceRepository;
import com.itsm.state.IncidentLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
    public class IncidentService {
        private final IncidentRepository incidentRepository;
        private final ItServiceRepository serviceRepository;
        private final AuditLogRepository auditLogRepository;
        private final IncidentLifecycle lifecycle;

        public IncidentService(IncidentRepository incidentRepository, ItServiceRepository serviceRepository, AuditLogRepository auditLogRepository, IncidentLifecycle lifecycle) {
            this.incidentRepository = incidentRepository;
            this.serviceRepository = serviceRepository;
            this.auditLogRepository = auditLogRepository;
            this.lifecycle = lifecycle;
        }

        public Incident findById(Long id) {
            return incidentRepository.findById(id).orElseThrow(() -> new IncidentNotFoundException(" Incident "+ id+" not found "));
        }
        public Incident create(IncidentForm form,Employee reporter){
            //get the service the employee picked
            ItService service = serviceRepository.findById(form.getServiceId()).orElseThrow(() -> new ServiceNotFoundException(" Service "+form.getServiceId()+" not found"));
            //build the incident object
            Incident incident = new Incident(form.getTitle(), form.getDescription(), reporter, service, form.getImpact(), form.getUrgency(), form.getAffectedUsers());

            Incident savedIncident = incidentRepository.save(incident);
                auditLogRepository.save(new AuditLog(reporter.getUsername(), "INCIDENT_CREATED",
                savedIncident.getId(), null, savedIncident.getStatus().name(),
                savedIncident.getReference() + " created: " + savedIncident.getTitle()));

                return savedIncident;
        }


    }

