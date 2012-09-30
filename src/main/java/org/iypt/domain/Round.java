package org.iypt.domain;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author jlocker
 */
public class Round {

    private int number;
    private int day;
    private Set<Group> groups;

    public Round(int number, int day) {
        this.number = number;
        this.day = day;
        this.groups = new LinkedHashSet<Group>(7);
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
     * Get the value of groups
     *
     * @return the value of groups
     */
    public Set<Group> getGroups() {
        return groups;
    }

    /**
     * Set the value of groups
     *
     * @param groups new value of groups
     */
    public void setGroups(Set<Group> groups) {
        this.groups = groups;
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
     * Get the value of number
     *
     * @return the value of number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Set the value of number
     *
     * @param number new value of number
     */
    public void setNumber(int number) {
        this.number = number;
    }
}
