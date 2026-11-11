package com.itsm.domain.user;

import com.itsm.domain.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** A member of staff who reports incidents. */
@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends User {

    @Column(length = 100)
    private String department;

    protected Employee() {
    }

    public Employee(String username, String passwordHash, String fullName,
                    String email, String department) {
        super(username, passwordHash, fullName, email);
        this.department = department;
    }

    @Override
    public Role getRole() {
        return Role.EMPLOYEE;
    }

    public String getDepartment() {
        return department;
    }

    public void changeDepartment(String department) {
        this.department = department;
    }
}
