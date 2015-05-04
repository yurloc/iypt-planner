package org.iypt.planner.gui;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.iypt.planner.domain.Round;
import org.iypt.planner.solver.TournamentSolver;

public class ScheduleModel {

    private final List<RoundModel> rounds;

    public ScheduleModel(TournamentSolver solver) {
        rounds = new ArrayList<>();
        for (Round round : solver.getTournament().getRounds()) {
            rounds.add(new RoundModel(solver, round));
        }
    }

    public List<RoundModel> getRounds() {
        return rounds;
    }
}
