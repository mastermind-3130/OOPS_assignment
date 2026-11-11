package com.itsm.repository;

import com.itsm.domain.user.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    List<Technician> findByAvailableTrue();
}
