package org.iypt.planner.gui;

import org.iypt.planner.domain.Group;

/**
 *
 * @author jlocker
 */
public class JurorAssignment {

    public enum Status {

        ASSIGNED, IDLE, AWAY
    }
    // TODO add seat?
    private final RoundModel round;
    private final Group group;
    private Status originalStatus;
    private Status currentStatus;

    public JurorAssignment(RoundModel round, Group group, Status status) {
        this.round = round;
        this.group = group;
        this.originalStatus = status;
        this.currentStatus = status;
    }

    public JurorAssignment(RoundModel round, boolean idle) {
        this(round, null, idle ? Status.IDLE : Status.AWAY);
    }

    public JurorAssignment(RoundModel round, Group group) {
        this(round, group, Status.ASSIGNED);
    }

    public RoundModel getRound() {
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
