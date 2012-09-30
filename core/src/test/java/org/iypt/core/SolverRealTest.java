package org.iypt.core;

import java.util.Collections;
import org.drools.planner.config.termination.TerminationConfig;
import org.iypt.domain.DayOff;
import org.iypt.domain.Round;
import org.iypt.domain.Tournament;
import org.iypt.domain.util.DefaultTournamentFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.iypt.domain.util.SampleFacts.*;
import static org.junit.Assert.*;

/**
 *
 * @author jlocker
 */
public class SolverRealTest extends AbstractSolverTest {
    
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
        factory.setJuryCapacity(4);
        Round r1 = factory.createRound(1, t01, t02, t03, t04, t05, t06, t07, t08, t09, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22, t23, t24);
        Round r2 = factory.createRound(2, t21, t01, t23, t24, t04, t02, t03, t07, t05, t06, t10, t08, t09, t13, t11, t12, t16, t14, t15, t19, t17, t18, t22, t20);
        Round r3 = factory.createRound(3, t20, t15, t01, t23, t18, t04, t02, t21, t07, t05, t24, t10, t08, t03, t13, t11, t06, t16, t14, t09, t19, t17, t12, t22);
        Round r4 = factory.createRound(4, t09, t01, t17, t12, t04, t20, t15, t07, t23, t18, t10, t02, t21, t13, t05, t24, t16, t08, t03, t19, t11, t06, t22, t14);
        Round r5 = factory.createRound(5, t14, t24, t01, t17, t03, t04, t20, t06, t07, t23, t09, t10, t02, t12, t13, t05, t15, t16, t08, t18, t19, t11, t21, t22);
        Tournament t = factory.newTournament();

        // TODO should be handled by the Factory?
        t.addJurors(jA1, jA2, jA3, jA4, jA5, jA6);
        t.addJurors(jB1, jB2, jB3, jB4);
        t.addJurors(jC1, jC2, jC3, jC4);
        t.addJurors(jD1, jD2);
        t.addJurors(jE1, jF1, jG1, jH1, jI1, jJ1, jK1, jL1, jY1, jZ1);
        t.addJurors(jM1, jM2, jM3, jM4, jM5, jM6);
        t.addJurors(jN1, jN2, jN3, jN4, jN5, jN6);
        t.addDayOffs(new DayOff(jE1, r1.getDay()), new DayOff(jE1, r3.getDay()));
        t.addDayOffs(new DayOff(jA1, r1.getDay()), new DayOff(jA1, r3.getDay()));
        t.addDayOffs(new DayOff(jA2, r2.getDay()), new DayOff(jA2, r3.getDay()));
        t.addDayOffs(new DayOff(jA3, r1.getDay()), new DayOff(jA3, r2.getDay()));
        t.addDayOffs(new DayOff(jA4, r4.getDay()), new DayOff(jA4, r5.getDay()));
        t.addDayOffs(new DayOff(jA5, r1.getDay()), new DayOff(jA5, r5.getDay()));
        t.addDayOffs(new DayOff(jN1, r1.getDay()), new DayOff(jN1, r2.getDay()));
        
        return t;
    }
}
