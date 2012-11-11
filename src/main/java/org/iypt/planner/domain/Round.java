package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 *
 * @author jlocker
 */
public class Round {

    private int number;
    private int day;
    private List<Group> groups;
    private double iCount = 0;

    void setOptimalIndependentCount(double optimalCount) {
        iCount = optimalCount;
    }

    public double getOptimalIndependentCount() {
        return iCount;
    }

    public Round(int number, int day) {
        this.number = number;
        this.day = day;
        this.groups = new ArrayList<>(10);
    }
    
    public Group createGroup(String name) {
        Group group = new Group(name);
        group.setRound(this);
        groups.add(group);
        return group;
    }
    
    public void addGroups(Group... groups) {
        for (Group g : groups) {
            this.groups.add(g);
            g.setRound(this);
        }
    }

    @Override
    public String toString() {
        return "Round #" + number;
    }

    /**
     * Get the value of number
     *
     * @return the value of number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Get the value of day
     *
     * @return the value of day
     */
    public int getDay() {
        return day;
    }

    /**
     * Set the value of day
     *
     * @param day new value of day
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * Get the value of groups
     *
     * @return the value of groups
     */
    public List<Group> getGroups() {
        return groups;
    }

}
