package org.iypt.planner.solver;

import java.util.Collections;
import org.drools.planner.config.termination.TerminationConfig;
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.DefaultTournamentFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.iypt.planner.domain.util.SampleFacts.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

/**
 *
 * @author jlocker
 */
public class SolverTest extends AbstractSolverTest {
    
    @Test
    public void test() {
        assertThat(getConstraintList(), is(Collections.EMPTY_LIST));
    }

    @Override
    TerminationConfig getTerminationConfig() {
        TerminationConfig config = new TerminationConfig();
        return config;
    }

    @Override
    Tournament getInitialSolution() {
        DefaultTournamentFactory factory = new DefaultTournamentFactory();
        Round r1 = factory.createRound(1, gABC, gDEF, gGHI);
        Round r2 = factory.createRound(2, gADG, gBEH, gCFI);
        Round r3 = factory.createRound(3, gAFH, gBDI, gCEG);
        Tournament t = factory.newTournament();
        t.changeJuryCapacity(6);

        // TODO should be handled by the Factory?
        t.addJurors(jA1, jA2, jA3, jA4, jA5, jA6);
        t.addJurors(jB1, jB2, jB3, jB4);
        t.addJurors(jC1, jC2, jC3, jC4);
        t.addJurors(jD1, jD2, jE1, jF1, jG1);
        t.addDayOffs(new DayOff(jE1, r1.getDay()), new DayOff(jE1, r3.getDay()));
        
        return t;
    }
}
