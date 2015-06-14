package org.iypt.planner.solver;

import java.io.IOException;
import org.drools.planner.config.termination.TerminationConfig;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.domain.Tournament;
import org.junit.BeforeClass;
import org.junit.Test;

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
