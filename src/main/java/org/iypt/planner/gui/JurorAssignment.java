package org.iypt.planner.gui;

import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;

/**
 *
 * @author jlocker
 */
public class JurorAssignment {

    public enum Status {

        ASSIGNED, IDLE, AWAY
    }
    private final Round round;
    private final Group group;
    private Status originalStatus;
    private Status currentStatus;

    public JurorAssignment(Round round, Group group, Status status) {
        this.round = round;
        this.group = group;
        this.originalStatus = status;
        this.currentStatus = status;
    }

    public JurorAssignment(Round round, boolean idle) {
        this(round, null, idle ? Status.IDLE : Status.AWAY);
    }

    public JurorAssignment(Group group) {
        this(group.getRound(), group, Status.ASSIGNED);
    }

    public Round getRound() {
        return round;
    }

    public Group getGroup() {
        return group;
    }

    public Status getOriginalStatus() {
        return originalStatus;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void change(Status status) {
        this.currentStatus = status;
    }

    public boolean isDirty() {
        return currentStatus != originalStatus;
    }

    public void reset() {
        currentStatus = originalStatus;
    }

    @Override
    public String toString() {
        return String.format("status: %s, group: %s", currentStatus, group);
    }
}
