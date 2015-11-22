package org.iypt.planner.api.domain;

import java.util.List;

public class Group {

    private final String name;
    private final List<Team> teams;

    public Group(String name, List<Team> teams) {
        this.name = name;
        this.teams = teams;
    }

    public String getName() {
        return name;
    }

    public List<Team> getTeams() {
        return teams;
    }
}
