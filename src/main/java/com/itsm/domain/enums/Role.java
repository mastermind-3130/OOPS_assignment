package com.itsm.domain.enums;

/**
 * The three user roles in the system.
 * Each role knows its Spring Security authority name and its dashboard page.
 */
public enum Role {
    EMPLOYEE("/employee/dashboard"),
    TECHNICIAN("/technician/dashboard"),
    ADMIN("/admin/dashboard");

    private final String dashboardPath;

    Role(String dashboardPath) {
        this.dashboardPath = dashboardPath;
    }

    /** Spring Security expects role authorities to start with "ROLE_". */
    public String authority() {
        return "ROLE_" + name();
    }

    public String getDashboardPath() {
        return dashboardPath;
    }
}
