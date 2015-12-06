package org.iypt.planner.api.io.bias;

import java.util.Collection;
import java.util.List;

public interface TournamentData {

    List<String> getTournaments();

    Collection<Juror> getJurors(String tournamentName);
}
