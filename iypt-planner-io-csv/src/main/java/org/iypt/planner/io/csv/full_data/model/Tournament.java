package org.iypt.planner.io.csv.full_data.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.iypt.planner.io.csv.full_data.readers.TournamentReader;

/**
 *
 * @author jlocker
 */
public class Tournament {

    private final TournamentReader.TournamentRow row;
    private final List<Fight> fights = new ArrayList<>();
    private final Map<String, Juror> jurors = new HashMap<>();
    private int marksRecorded = 0;

    public Tournament(TournamentReader.TournamentRow row) {
        this.row = row;
    }

    public String getName() {
        return row.getTournament_name();
    }

    public boolean addFight(Fight fight) {
        return fights.add(fight);
    }

    public List<Fight> getFights() {
        return Collections.unmodifiableList(fights);
    }

    public void addJuror(Juror juror) {
        jurors.put(juror.getName(), juror);
    }

    public Collection<Juror> getJurors() {
        return jurors.values();
    }

    public Juror getJuror(String name) {
        return jurors.get(name);
    }

    public void calculate() {
        for (Fight fight : fights) {
            fight.calculate();
        }
    }

    void markRecorded() {
        marksRecorded++;
    }

    public int getMarksRecorded() {
        return marksRecorded;
    }

    @Override
    public String toString() {
        return getName();
    }
}
