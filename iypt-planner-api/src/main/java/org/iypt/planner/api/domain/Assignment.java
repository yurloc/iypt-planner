package org.iypt.planner.api.domain;

public class Assignment {

    private final Juror juror;
    private final Group group;
    private final Role role;

    public Assignment(Juror juror, Group group, Role role) {
        this.juror = juror;
        this.group = group;
        this.role = role;
    }

    public Group getGroup() {
        return group;
    }

    public Juror getJuror() {
        return juror;
    }

    public Role getRole() {
        return role;
    }
}
