package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public final class Jury {

    public static final int DEFAULT_CAPACITY = 6;
    private int capacity;
    private Group group;

    public Jury() {
        setCapacity(DEFAULT_CAPACITY);
    }

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

    /**
     * Get the value of capacity
     *
     * @return the value of capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Set the value of capacity
     *
     * @param capacity new value of capacity
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
