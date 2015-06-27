package org.iypt.planner.solver;

import java.io.IOException;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.junit.BeforeClass;
import org.junit.Test;
import org.optaplanner.core.config.termination.TerminationConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jlocker
 */
public class Solver2012Test extends AbstractSolverTest {

    private static Tournament tournament;

    @BeforeClass
    public static void setupTournament() throws IOException {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jury_data.csv");
        tournament = factory.newTournament();
        for (Round round : tournament.getRounds()) {
            tournament.changeJurySize(round, 6);
        }
    }

    @Test
    public void test() {
        assertThat(getBestSolution().getScore().getHardScore()).isZero();
    }

    @Override
    TerminationConfig getTerminationConfig() {
        return new TerminationConfig();
    }

    @Override
    Tournament getInitialSolution() {
        return tournament;
    }
}
