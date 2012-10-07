package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jlocker
 */
public class Jury {

    private int capacity;
    private Group group;
    private Juror chair;
    private List<JurySeat> seats;

    @Override
    public String toString() {
        return group.toString();
    }

    //=========================================================================================================================
    // Getters & Setters
    //=========================================================================================================================

    public List<JurySeat> getSeats() {
        return seats;
    }

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
        seats = new ArrayList<>(capacity);
    }

    /**
     * Get the value of chair
     *
     * @return the value of chair
     */
    public Juror getChair() {
        return chair;
    }

    /**
     * Set the value of chair
     *
     * @param chair new value of chair
     */
    public void setChair(Juror chair) {
        this.chair = chair;
    }

}
