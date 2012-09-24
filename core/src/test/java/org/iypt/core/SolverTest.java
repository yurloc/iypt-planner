package org.iypt.core;

import org.drools.planner.config.termination.TerminationConfig;
import org.iypt.domain.DayOff;
import org.iypt.domain.Round;
import org.iypt.domain.Tournament;
import org.iypt.domain.util.DefaultTournamentFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.iypt.core.TestFacts.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

/**
 *
 * @author jlocker
 */
public class SolverTest extends AbstractSolverTest {
    
    @Test
    public void test() {
        assertFalse(getBestSolution().getJurors().isEmpty());
        assertFalse(getBestSolution().getJuries().isEmpty());
        assertFalse(getBestSolution().getJuryMemberships().isEmpty());
    }

    @Override
    TerminationConfig getTerminationConfig() {
        TerminationConfig config = new TerminationConfig();
        config.setTerminationCompositionStyle(TerminationConfig.TerminationCompositionStyle.OR);
        config.setMaximumSecondsSpend(2L);
//        config.setScoreAttained(null);
        return config;
    }

    @Override
    Tournament getInitialSolution() {
        DefaultTournamentFactory factory = new DefaultTournamentFactory();
        factory.setJuryCapacity(6);
        Round r1 = factory.createRound(1, gABC, gDEF, gGHI);
        Round r2 = factory.createRound(2, gADG, gBEH, gCFI);
        Round r3 = factory.createRound(3, gAFH, gBDI, gCEG);
        Tournament t = factory.newTournament();

        // TODO should be handled by the Factory?
        t.addJurors(jA1, jB1, jC1, jD1, jE1, jF1, jC2, jC3, jC2, jA2, jA3, jB2, jB3);
        t.addJurors(jB4, jC4, jD2, jA4, jA5, jA6);
        t.addDayOffs(new DayOff(jE1, r1.getDay()), new DayOff(jE1, r3.getDay()));
        
        return t;
    }
}
