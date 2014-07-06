package org.iypt.planner.solver;

import org.drools.planner.config.termination.TerminationConfig;
import org.iypt.planner.domain.Absence;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.RoundFactory;
import org.iypt.planner.domain.Tournament;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iypt.planner.domain.SampleFacts.*;

/**
 *
 * @author jlocker
 */
public class SolverMinimalTest extends AbstractSolverTest {

    @Test
    public void test() {
        assertThat(getBestSolution().getScore().getHardScore()).isZero();
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
        t.addJurors(jD1, jD2, jE1, jE2, jF1, jF2, jG1, jH1, jI1);
        t.addJurors(jM2, jM3, jM4, jM5, jM6);

        t.addAbsences(new Absence(jE1, r1), new Absence(jE1, r3));
        t.addAbsences(new Absence(jH1, r2), new Absence(jI1, r2));

        // just visualizing the numbers
        assertThat(t.getSeats().size() / t.getRounds().size()).isEqualTo(18);
        assertThat(t.getJurors().size()).isEqualTo(28);

        return t;
    }
}
