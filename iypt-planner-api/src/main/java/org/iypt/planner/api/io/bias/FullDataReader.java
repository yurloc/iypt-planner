package org.iypt.planner.api.io.bias;

import java.io.IOException;
import java.io.Reader;

public interface FullDataReader {

    TournamentData readData(Reader reader) throws IOException;
}
