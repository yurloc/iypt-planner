package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jlocker
 */
public class Round {

    private final int number;
    private final List<Group> groups;
    private double iCount = 0;

    void setOptimalIndependentCount(double optimalCount) {
        iCount = optimalCount;
    }

    public double getOptimalIndependentCount() {
        return iCount;
    }

    public Round(int number) {
        this.number = number;
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
     * Get round number.
     *
     * @return number of the round, should be 1 - 5
     */
    public int getNumber() {
        return number;
    }

    /**
     * Get groups in this round.
     *
     * @return list of groups in this round
     */
    public List<Group> getGroups() {
        return groups;
    }
}
