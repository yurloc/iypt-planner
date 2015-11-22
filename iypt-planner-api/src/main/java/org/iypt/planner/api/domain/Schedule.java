package org.iypt.planner.api.domain;

import java.util.List;

public class Schedule {

    private final Tournament tournament;
    private final List<Assignment> assignments;

    public Schedule(Tournament tournament, List<Assignment> assignments) {
        this.tournament = tournament;
        this.assignments = assignments;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public Tournament getTournament() {
        return tournament;
    }
}
