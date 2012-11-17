package org.iypt.planner.gui;

import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;

/**
 *
 * @author jlocker
 */
public class JurorDay {

    public enum Status {

        ASSIGNED, IDLE, AWAY
    }
    private Round round;
    private Group group;
    private Status status;
    private Status change;

    public JurorDay(Round round, Group group, Status status) {
        this.round = round;
        this.group = group;
        this.status = status;
        this.change = status;
    }

    public JurorDay(Round round, boolean idle) {
        this(round, null, idle ? Status.IDLE : Status.AWAY);
    }

    public JurorDay(Group group) {
        this(group.getRound(), group, Status.ASSIGNED);
    }

    public Round getRound() {
        return round;
    }

    public Status getStatus() {
        return status;
    }

    public Group getGroup() {
        return group;
    }

    boolean change(Status status) {
        change = status;
        return isDirty();
    }

    public boolean isDirty() {
        return status != change;
    }

    public Status getChange() {
        return change;
    }

    public void reset() {
        change = status;
    }

    public void applyChange() {
        status = change;
    }

    @Override
    public String toString() {
        return String.format("status: %s, group: %s", status, group);
    }
}
