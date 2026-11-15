package com.itsm.service;

import com.itsm.domain.incident.Incident;
import com.itsm.dto.IncidentSearchCriteria;
import com.itsm.repository.IncidentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class IncidentSearchService {

    private final IncidentRepository incidentRepository;

    public IncidentSearchService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<Incident> search(IncidentSearchCriteria c) {
        Specification<Incident> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (c.getStatus() != null) {
                p.add(cb.equal(root.get("status"), c.getStatus()));
            }
            if (c.getPriority() != null) {
                p.add(cb.equal(root.get("priority"), c.getPriority()));
            }
            if (c.getCategoryId() != null) {
                p.add(cb.equal(root.get("category").get("id"), c.getCategoryId()));
            }
            if (c.getServiceId() != null) {
                p.add(cb.equal(root.get("service").get("id"), c.getServiceId()));
            }
            if (c.getTechnicianId() != null) {
                p.add(cb.equal(root.get("assignedTechnician").get("id"), c.getTechnicianId()));
            }
            if (c.getDateFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), c.getDateFrom().atStartOfDay()));
            }
            if (c.getDateTo() != null) {
                // include the whole "to" day
                p.add(cb.lessThan(root.get("createdAt"), c.getDateTo().plusDays(1).atStartOfDay()));
            }
            if (Boolean.TRUE.equals(c.getSlaBreached())) {
                p.add(cb.or(cb.isTrue(root.get("responseBreached")),
                            cb.isTrue(root.get("resolutionBreached"))));
            }
            // reference such as "INC0007" or "7": strip non-digits, parse to Long, compare with id
            if (c.getReference() != null && !c.getReference().isBlank()) {
                String digits = c.getReference().replaceAll("\\D", "");
                if (digits.isEmpty() || digits.length() > 18) {
                    p.add(cb.disjunction()); // nothing can match
                } else {
                    p.add(cb.equal(root.get("id"), Long.parseLong(digits)));
                }
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return incidentRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}