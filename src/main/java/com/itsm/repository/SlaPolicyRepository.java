package com.itsm.repository;

import com.itsm.domain.catalog.ItService;
import com.itsm.domain.catalog.SlaPolicy;
import com.itsm.domain.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    /** Default policy for a priority (applies to all services). */
    Optional<SlaPolicy> findByPriorityAndServiceIsNull(Priority priority);

    /** Service-specific override, if one exists. */
    Optional<SlaPolicy> findByPriorityAndService(Priority priority, ItService service);
}
