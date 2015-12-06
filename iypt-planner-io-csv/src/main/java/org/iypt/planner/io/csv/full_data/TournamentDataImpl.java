package org.iypt.planner.io.csv.full_data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.iypt.planner.api.io.bias.Juror;
import org.iypt.planner.api.io.bias.TournamentData;
import org.iypt.planner.io.csv.full_data.model.Fight;
import org.iypt.planner.io.csv.full_data.model.Tournament;

public class TournamentDataImpl implements TournamentData {

    private final Map<Integer, Tournament> tournamentsById;
    private final Map<String, Tournament> tournamentsByName;
    private final Map<Integer, Fight> fights;
    private final Map<Integer, org.iypt.planner.io.csv.full_data.model.Juror> jurors;

    public TournamentDataImpl(Map<Integer, Tournament> tournaments, Map<Integer, Fight> fights, Map<Integer, org.iypt.planner.io.csv.full_data.model.Juror> jurors) {
        this.tournamentsById = tournaments;
        this.fights = fights;
        this.jurors = jurors;
        this.tournamentsByName = new HashMap<>();
        for (Tournament t : tournamentsById.values()) {
            tournamentsByName.put(t.getName(), t);
        }
    }

    @Override
    public List<String> getTournaments() {
        List<String> tournaments = new ArrayList<>();
        tournaments.addAll(tournamentsByName.keySet());
        return tournaments;
    }

    @Override
    public Collection<Juror> getJurors(String tournamentName) {
        // FIXME make this method idempotent
        Tournament tournament = tournamentsByName.get(tournamentName);
        tournament.calculate();
        List<Juror> jurors = new ArrayList<>();
        for (org.iypt.planner.io.csv.full_data.model.Juror juror : tournament.getJurors()) {
            jurors.add(new Juror(juror.getGivenName(), juror.getLastName(), juror.getAverageBias(), juror.isJuror()));
        }
        return jurors;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    // For testing
    //--------------------------------------------------------------------------------------------------------------------------
    //
    Tournament getTournament(int id) {
        return tournamentsById.get(id);
    }

    Fight getFight(int id) {
        return fights.get(id);
    }

    org.iypt.planner.io.csv.full_data.model.Juror getJuror(int id) {
        return jurors.get(id);
    }
}
