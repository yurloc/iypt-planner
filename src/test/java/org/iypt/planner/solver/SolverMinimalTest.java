package org.iypt.planner.solver;

import java.util.Collections;
import org.drools.planner.config.termination.TerminationConfig;
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.RoundFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.iypt.planner.domain.util.SampleFacts.*;
import static org.junit.Assert.*;

/**
 *
 * @author jlocker
 */
public class SolverMinimalTest extends AbstractSolverTest {

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
        Round r1 = RoundFactory.createRound(1, gABC, gDEF, gGHI);
        Round r2 = RoundFactory.createRound(2, gADG, gBEH, gCFI);
        Round r3 = RoundFactory.createRound(3, gAFH, gBDI, gCEG);
        Tournament t = new Tournament();
        t.setJuryCapacity(6);
        t.addRounds(r1, r2, r3);

        t.addJurors(jA1, jA2, jA3, jA4, jA5, jA6);
        t.addJurors(jB1, jB2, jB3, jB4);
        t.addJurors(jC1, jC2, jC3, jC4);
        t.addJurors(jD1, jD2, jE1, jE2, jF1, jF2, jG1);

        t.addDayOffs(new DayOff(jE1, r1.getDay()), new DayOff(jE1, r3.getDay()));
        
        // just visualizing the numbers
        assertThat(t.getJurySeats().size() / t.getRounds().size(), is(18));
        assertThat(t.getJurors().size(), is(21));

        return t;
    }
}