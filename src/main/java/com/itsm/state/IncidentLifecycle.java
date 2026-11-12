package com.itsm.state;
import com.itsm.domain.incident.Incident;
import com.itsm.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;
import com.itsm.domain.enums.IncidentStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class IncidentLifecycle{
    private final Map<IncidentStatus, Set<IncidentStatus>> allowed = new EnumMap<>(IncidentStatus.class);

    //constructor
    public IncidentLifecycle(){
        allowed.put(IncidentStatus.NEW,EnumSet.of(IncidentStatus.CLASSIFIED, IncidentStatus.ASSIGNED));
        allowed.put(IncidentStatus.CLASSIFIED,EnumSet.of(IncidentStatus.ASSIGNED));
        allowed.put(IncidentStatus.ASSIGNED,EnumSet.of(IncidentStatus.ACKNOWLEDGED));
        allowed.put(IncidentStatus.ACKNOWLEDGED, EnumSet.of(IncidentStatus.IN_PROGRESS));
        allowed.put(IncidentStatus.IN_PROGRESS,EnumSet.of(IncidentStatus.PENDING_INFO, IncidentStatus.RESOLVED));
        allowed.put(IncidentStatus.PENDING_INFO,EnumSet.of(IncidentStatus.IN_PROGRESS));
        allowed.put(IncidentStatus.RESOLVED,EnumSet.of(IncidentStatus.VERIFIED, IncidentStatus.IN_PROGRESS));
        allowed.put(IncidentStatus.VERIFIED, EnumSet.of(IncidentStatus.CLOSED));
        allowed.put(IncidentStatus.CLOSED,EnumSet.noneOf(IncidentStatus.class));

    }
    //checking method
    public boolean canMove(IncidentStatus from,IncidentStatus to){
        return allowed.get(from).contains(to);
    }
    public void moveTo(Incident incident, IncidentStatus next){
        if(!canMove(incident.getStatus(),next))
        {
          throw new InvalidStateTransitionException("Cannot move "+incident.getReference()+" from "+ incident.getStatus() +" to "+next);
        }
        incident.changeStatus(next);        
    }
}