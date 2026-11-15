package com.itsm.dto;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Priority;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class IncidentSearchCriteria {

    private String reference;
    private IncidentStatus status;
    private Priority priority;
    private Long categoryId;
    private Long serviceId;
    private Long technicianId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private Boolean slaBreached;

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
    public Boolean getSlaBreached() { return slaBreached; }
    public void setSlaBreached(Boolean slaBreached) { this.slaBreached = slaBreached; }
}