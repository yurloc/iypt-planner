package org.iypt.planner.api.domain;

import java.util.List;

public class Round {

    private int number;
    private List<Group> groups;

    public Round(int number, List<Group> groups) {
        this.number = number;
        this.groups = groups;
    }

    public int getNumber() {
        return number;
    }

    public List<Group> getGroups() {
        return groups;
    }
}
