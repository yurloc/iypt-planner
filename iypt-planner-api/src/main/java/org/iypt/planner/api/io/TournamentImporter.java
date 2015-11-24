package org.iypt.planner.api.io;

import java.io.IOException;
import org.iypt.planner.api.domain.BiasData;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.domain.Tournament;

public interface TournamentImporter {

    Tournament loadTournament(InputSource teamsSource, InputSource jurorsSource) throws IOException;

    Schedule loadSchedule(Tournament tournament, InputSource scheduleSource) throws IOException;

    BiasData loadBiases(Tournament tournament, InputSource biasSource) throws IOException;
}
