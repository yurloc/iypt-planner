package org.iypt.planner.opta.drools.solver;

import org.iypt.planner.opta.drools.domain.Absence;
import org.iypt.planner.opta.drools.domain.Round;
import org.iypt.planner.opta.drools.domain.RoundFactory;
import org.iypt.planner.opta.drools.domain.Tournament;
import org.junit.Test;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iypt.planner.opta.drools.domain.SampleFacts.*;

/**
 *
 * @author jlocker
 */
public class SolverLargeTest extends AbstractSolverTest {

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
        Round r1 = RoundFactory.createRound(1, t01, t02, t03, t04, t05, t06, t07, t08, t09, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22, t23, t24);
        Round r2 = RoundFactory.createRound(2, t21, t01, t23, t24, t04, t02, t03, t07, t05, t06, t10, t08, t09, t13, t11, t12, t16, t14, t15, t19, t17, t18, t22, t20);
        Round r3 = RoundFactory.createRound(3, t20, t15, t01, t23, t18, t04, t02, t21, t07, t05, t24, t10, t08, t03, t13, t11, t06, t16, t14, t09, t19, t17, t12, t22);
        Round r4 = RoundFactory.createRound(4, t09, t01, t17, t12, t04, t20, t15, t07, t23, t18, t10, t02, t21, t13, t05, t24, t16, t08, t03, t19, t11, t06, t22, t14);
        Round r5 = RoundFactory.createRound(5, t14, t24, t01, t17, t03, t04, t20, t06, t07, t23, t09, t10, t02, t12, t13, t05, t15, t16, t08, t18, t19, t11, t21, t22);
        r1.setJurySize(4);
        r2.setJurySize(4);
        r3.setJurySize(4);
        r4.setJurySize(4);
        r5.setJurySize(4);
        Tournament t = new Tournament();
        t.addRounds(r1, r2, r3, r4, r5);

        t.addJurors(jA1, jA2, jA3, jA4, jA5, jA6);
        t.addJurors(jB1, jB2, jB3, jB4);
        t.addJurors(jC1, jC2, jC3, jC4);
        t.addJurors(jD1, jD2);
        t.addJurors(jE1, jF1, jG1, jH1, jI1, jJ1, jK1, jL1, jY1, jZ1);
        t.addJurors(jM1, jM2, jM3, jM4, jM5, jM6);
        t.addJurors(jN1, jN2, jN3, jN4, jN5, jN6);
        t.addAbsences(new Absence(jE1, r1), new Absence(jE1, r3));
        t.addAbsences(new Absence(jA1, r1), new Absence(jA1, r3));
        t.addAbsences(new Absence(jA2, r2), new Absence(jA2, r3));
        t.addAbsences(new Absence(jA3, r1), new Absence(jA3, r2));
        t.addAbsences(new Absence(jA4, r4), new Absence(jA4, r5));
        t.addAbsences(new Absence(jA5, r1), new Absence(jA5, r5));
        t.addAbsences(new Absence(jN1, r1), new Absence(jN1, r2));

        // visualizing the numbers
        assertThat(t.getJurors().size()).isEqualTo(38);
        assertThat((t.getSeats().size() - Tournament.NON_VOTING_SEAT_BUFFER * t.getJuries().size())
                / t.getRounds().size()).isEqualTo(32);

        assertThat(t.getAbsencesPerRound(r1)).isEqualTo(5);
        assertThat(t.getAbsencesPerRound(r2)).isEqualTo(3);
        assertThat(t.getAbsencesPerRound(r3)).isEqualTo(3);
        assertThat(t.getAbsencesPerRound(r4)).isEqualTo(1);
        assertThat(t.getAbsencesPerRound(r5)).isEqualTo(2);

        return t;
    }
}
