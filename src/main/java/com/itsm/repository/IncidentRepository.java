package com.itsm.repository;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Priority;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Employee;
import com.itsm.domain.user.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

/**
 * JpaSpecificationExecutor lets the search feature combine any number of
 * optional filters (status, priority, category, date, ...) at runtime.
 */
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    List<Incident> findByReporterOrderByCreatedAtDesc(Employee reporter);

    List<Incident> findByAssignedTechnicianOrderByCreatedAtDesc(Technician technician);

    long countByAssignedTechnicianAndStatusIn(Technician technician, Collection<IncidentStatus> statuses);

    long countByStatusIn(Collection<IncidentStatus> statuses);

    long countByPriorityAndStatusIn(Priority priority, Collection<IncidentStatus> statuses);
}
