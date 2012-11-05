package org.iypt.planner.csv;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.iypt.planner.csv.MarkReader.MarkRow;

/**
 *
 * @author jlocker
 */
public class Fight {

    private final FightReader.FightRow row;
    private final Tournament tournament;
    private final Map<Integer, Stage> stages = new HashMap<>();
    private final Map<Integer, Juror> jurors = new HashMap<>();

    Fight(FightReader.FightRow row, Map<Integer, Tournament> tournaments) {
        this.row = row;
        this.tournament = tournaments.get(row.getTournament());
        if (this.tournament == null) {
            throw new IllegalArgumentException("Cannot find tournament with id " + row.getTournament());
        }
    }

    Tournament getTournament() {
        return tournament;
    }

    Stage getStage(int stageNumber) {
        Stage stage = stages.get(stageNumber);
        if (stage == null) {
            stage = new Stage(this, stageNumber);
            stages.put(stageNumber, stage);
        }
        return stage;
    }

    void assignJuror(int juror_number, Juror juror) {
        jurors.put(juror_number, juror);
    }

    public Collection<Juror> getJurors() {
        return jurors.values();
    }

    void recordMark(MarkRow row) {
        Stage stage = getStage(row.getStage());
        Juror juror = jurors.get(row.getJuror_number());
        stage.markRole(juror, row.getRole(), row.getMark());
        tournament.markRecorded();
    }

    void calculate() {
        for (Stage stage : stages.values()) {
            stage.calculate();
        }
    }

    @Override
    public String toString() {
        return String.format("%s, Round #%d, %s", tournament.getName(), row.getRound(), row.getGroup_name());
    }

    public class Stage {

        private JudgingEvent[] events = new JudgingEvent[3];

        public Stage(Fight fight, int number) {
            // each stage has 3 judging events (for each of the 3 active roles)
            for (int i = 0; i < events.length; i++) {
                events[i] = new JudgingEvent(fight, number, i + 1);
            }
        }

        public JudgingEvent getJudgingEvent(int role) {
            return events[role - 1];
        }

        void markRole(Juror juror, int role, int mark) {
            events[role - 1].addMark(juror, mark);
        }

        void calculate() {
            for (JudgingEvent event : events) {
                event.calculate();
            }
        }
    }
}
