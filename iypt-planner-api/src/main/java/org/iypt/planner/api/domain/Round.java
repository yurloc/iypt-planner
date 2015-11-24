package org.iypt.planner.api.domain;

import java.util.List;

public class Round {

    private final int number;
    private final List<Group> groups;
    private int jurySize;

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

    public int getJurySize() {
        return jurySize;
    }

    public void setJurySize(int jurySize) {
        this.jurySize = jurySize;
    }
}
