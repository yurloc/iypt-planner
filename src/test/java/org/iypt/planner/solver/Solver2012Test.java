package org.iypt.planner.solver;

import java.io.IOException;
import java.util.Collections;
import org.drools.planner.config.termination.TerminationConfig;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.CSVTournamentFactory;
import org.iypt.planner.gui.PlannerWindow;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author jlocker
 */
public class Solver2012Test extends AbstractSolverTest {

    private static Tournament tournament;

    @BeforeClass
    public static void setupTournament() throws IOException {
        String path = "/org/iypt/planner/csv/";
        CSVTournamentFactory factory = new CSVTournamentFactory(PlannerWindow.class, path + "team_data.csv", path + "jury_data.csv");
        tournament = factory.newTournament();
        tournament.setJuryCapacity(6);
    }

    @Test
    public void test() {
        assertThat(getConstraintList(), is(Collections.EMPTY_LIST));
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
