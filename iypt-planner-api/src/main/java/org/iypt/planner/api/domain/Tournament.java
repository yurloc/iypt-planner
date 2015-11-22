package org.iypt.planner.api.domain;

import java.util.List;

public class Tournament {

    private final List<Round> rounds;

    public Tournament(List<Round> rounds) {
        this.rounds = rounds;
    }

    public List<Round> getRounds() {
        return rounds;
    }
}
