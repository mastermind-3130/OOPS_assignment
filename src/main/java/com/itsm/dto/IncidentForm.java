package com.itsm.dto;

import com.itsm.domain.enums.Impact;
import com.itsm.domain.enums.Urgency;
import jakarta.validation.constraints.*;

public class IncidentForm {

    @NotBlank(message="Title is required")
    @Size(max=150,message="Title shouldn't be longer than 150 characters")
    private String title;
    @NotBlank(message ="Description is required")
    private String description;
    @NotNull(message="Please choose a service")
    private Long serviceId;
    @NotNull(message="Please choose an Impact")
    private Impact impact;
    @NotNull(message="Please choose an Urgency")
    private Urgency urgency;
    @NotNull(message="Please select number of affected users")
    @Min(value=1,message="Atleast 1 uesr must be affected")
    private Integer affectedUsers=1 ;
        public String getTitle() {
        return title;
    }

    //getters and setters
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Impact getImpact() {
        return impact;
    }

    public void setImpact(Impact impact) {
        this.impact = impact;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public Integer getAffectedUsers() {
        return affectedUsers;
    }

    public void setAffectedUsers(Integer affectedUsers) {
        this.affectedUsers = affectedUsers;
    }
}
