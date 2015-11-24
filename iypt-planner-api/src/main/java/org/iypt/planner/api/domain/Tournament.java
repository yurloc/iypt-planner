package org.iypt.planner.api.domain;

import java.util.List;

public class Tournament {

    private final List<Round> rounds;
    private final List<Juror> jurors;

    public Tournament(List<Round> rounds, List<Juror> jurors) {
        this.rounds = rounds;
        this.jurors = jurors;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public List<Juror> getJurors() {
        return jurors;
    }
}
