package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.util.HashMap;
import java.util.Map;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.iypt.planner.domain.Absence;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorLoad;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.solver.TournamentSolver;

public class ScheduleModel {

    private final Map<Juror, java.util.List<JurorAssignment>> jurorAssignmentMap = new HashMap<>();
    private final Map<Juror, java.util.List<CountryCode>> conflictMap;
    private final Map<Juror, JurorLoad> loadMap;
    private final List<RoundModel> rounds;

    public ScheduleModel(TournamentSolver solver, Map<Juror, java.util.List<CountryCode>> conflictMap, Map<Juror, JurorLoad> loadMap) {
        this.conflictMap = conflictMap;
        this.loadMap = loadMap;
        Tournament tournament = solver.getTournament();

        rounds = new ArrayList<>();
        for (Round round : solver.getTournament().getRounds()) {
            RoundModel roundModel = new RoundModel(solver, round);
            rounds.add(roundModel);

            for (Juror juror : tournament.getJurors()) {
                if (!jurorAssignmentMap.containsKey(juror)) {
                    jurorAssignmentMap.put(juror, new java.util.ArrayList<JurorAssignment>(tournament.getRounds().size()));
                }
                // idle all rounds by default
                jurorAssignmentMap.get(juror).add(round.getNumber() - 1, new JurorAssignment(roundModel, true));
            }

            java.util.List<Juror> idleList = new java.util.ArrayList<>();
            java.util.List<Juror> awayList = new java.util.ArrayList<>();
            idleList.addAll(tournament.getJurors());
            for (Seat seat : tournament.getSeats()) {
                if (seat.isOccupied() && seat.getJury().getGroup().getRound().equals(round)) {
                    idleList.remove(seat.getJuror());
                    jurorAssignmentMap.get(seat.getJuror())
                            .set(round.getNumber() - 1, new JurorAssignment(roundModel, seat.getJury().getGroup()));
                }
            }
            for (Absence absence : tournament.getAbsences()) {
                if (absence.getRoundNumber() == round.getNumber()) {
                    awayList.add(absence.getJuror());
                    jurorAssignmentMap.get(absence.getJuror())
                            .set(round.getNumber() - 1, new JurorAssignment(roundModel, false));
                }
            }
            idleList.removeAll(awayList); // idle = all -busy -away

            List<SeatInfo> aw = new ArrayList<>();
            List<SeatInfo> id = new ArrayList<>();
            for (Juror j : awayList) {
                aw.add(SeatInfo.newInstance(j));
            }
            for (Juror j : idleList) {
                id.add(SeatInfo.newInstance(j));
            }
            roundModel.setAway(aw);
            roundModel.setIdle(id);
        }
    }

    public List<RoundModel> getRounds() {
        return rounds;
    }

    public JurorInfo getJurorInfo(Juror juror) {
        return new JurorInfo(juror, conflictMap.get(juror), jurorAssignmentMap.get(juror), loadMap.get(juror));
    }
}
