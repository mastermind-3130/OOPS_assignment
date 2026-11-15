package com.itsm.assignment;

import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Technician;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SkillBasedAssignment implements AssignmentStrategy {
    @Override
    public String getName() { return "SKILL_BASED"; }

    @Override
    public String getLabel() { return "Skill based"; }

    @Override
    public Optional<Technician> select(Incident incident,
                                       List<Technician> candidates,
                                       Map<Long, Long> activeCounts) {
        if (incident.getCategory() == null) {
            return Optional.empty();
        }

        return candidates.stream()
                .filter(t -> t.hasSkill(incident.getCategory().getCode()))
                .min((a, b) -> {
                    int loadCompare = Long.compare(
                            activeCounts.getOrDefault(a.getId(), 0L),
                            activeCounts.getOrDefault(b.getId(), 0L));
                    return loadCompare != 0 ? loadCompare : a.getId().compareTo(b.getId());
                });
    }
}
