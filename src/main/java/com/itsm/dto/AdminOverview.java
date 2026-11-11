package com.itsm.dto;

/** Numbers shown on the admin dashboard. */
public record AdminOverview(long totalIncidents,
                            long openIncidents,
                            long criticalOpenIncidents,
                            long resolvedIncidents,
                            long slaViolations,
                            long totalUsers) {
}
