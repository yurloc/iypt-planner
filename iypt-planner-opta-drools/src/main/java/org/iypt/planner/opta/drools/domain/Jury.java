package org.iypt.planner.opta.drools.domain;

/**
 *
 * @author jlocker
 */
public final class Jury {

    private Group group;

    protected String coords() {
        return group.coords();
    }

    @Override
    public String toString() {
        return String.format("Jury %s", coords());
    }

    /**
     * Get the group this jury is assigned to.
     *
     * @return the group this jury is assigned to
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Set the group this jury is assigned to.
     *
     * @param group the group this jury is assigned to
     */
    public void setGroup(Group group) {
        this.group = group;
    }
}
