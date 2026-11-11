package com.itsm.repository;

import com.itsm.domain.incident.Incident;
import com.itsm.domain.incident.SlaViolation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlaViolationRepository extends JpaRepository<SlaViolation, Long> {
    List<SlaViolation> findByIncident(Incident incident);
}
