package com.itsm.repository;

import com.itsm.domain.catalog.ItService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItServiceRepository extends JpaRepository<ItService, Long> {
    Optional<ItService> findByName(String name);
    List<ItService> findByActiveTrueOrderByNameAsc();
}
