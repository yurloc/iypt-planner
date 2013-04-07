package org.iypt.planner.domain;

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

    // ------------------------------------------------------------------------
    // Getters & Setters
    // ------------------------------------------------------------------------
    /**
     * Get the value of group
     *
     * @return the value of group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Set the value of group
     *
     * @param group new value of group
     */
    public void setGroup(Group group) {
        this.group = group;
    }
}
