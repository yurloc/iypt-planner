package org.iypt.planner.gui;

import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;

/**
 *
 * @author jlocker
 */
public class JurorDay {
    public enum Status {
        AWAY, IDLE, ASSIGNED
    }
    private Round round;
    private Status status;
    private Group group;

    public JurorDay(Round round, boolean idle) {
        this.round = round;
        this.status = idle ? Status.IDLE : Status.AWAY;
    }

    public JurorDay(Group group) {
        this.round = group.getRound();
        this.status = Status.ASSIGNED;
        this.group = group;
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

    @Override
    public String toString() {
        return String.format("status: %s, group: %s", status, group);
    }
}
