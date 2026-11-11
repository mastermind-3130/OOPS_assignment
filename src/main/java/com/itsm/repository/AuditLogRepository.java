package com.itsm.repository;

import com.itsm.domain.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByIncidentIdOrderByTimestampAsc(Long incidentId);
    List<AuditLog> findTop10ByOrderByTimestampDesc();
}
