package org.iypt.planner.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

/**
 *
 * @author jlocker
 */
public class TournamentDataTest {

    private static final Logger log = LoggerFactory.getLogger(TournamentDataTest.class);
    private static TournamentData data;
    private static final float ERROR = 0.005f;

    @BeforeClass
    public static void setUp() throws IOException {
        data = new TournamentData();
        data.readData(new InputStreamReader(ReadersTest.class.getResourceAsStream("full_data.csv")));
    }

    @Test
    public void testData() {
        Tournament t = data.getTournament(9);
        assertThat(t.getName(), is("IYPT2012"));

        // check a few rows to see the data has been read correctly
        Juror jurorMP = data.getJuror(9420);
        Juror jurorPM = data.getJuror(9437);
        Juror jurorCL = data.getJuror(9419);
        assertThat(jurorMP.getName(), is("Martin Plesch"));
        assertThat(jurorPM.getName(), is("Prapun Manyum"));
        assertThat(jurorCL.getName(), is("Chuanyong Li"));

        Fight fight1A = data.getFight(1376);
        Fight fight1G = data.getFight(1382);
        Fight fightFinal = data.getFight(1453);

        // all these fights are from IYPT2012
        assertThat(fight1A.getTournament(), sameInstance(t));
        assertThat(fight1G.getTournament(), sameInstance(t));
        assertThat(fightFinal.getTournament(), sameInstance(t));

        // and are reachable from it
        assertThat(t.getFights(), hasItem(fight1A));
        assertThat(t.getFights(), hasItem(fight1G));
        assertThat(t.getFights(), hasItem(fightFinal));

        // check the right jurors are assigned to final fight
        assertThat(fightFinal.getJurors(), hasItems(jurorMP, jurorPM, jurorCL));

        t.calculate();

        JudgingEvent event1 = fight1G.getStage(3).getJudgingEvent(1);
        JudgingEvent event2 = fightFinal.getStage(3).getJudgingEvent(3);

        log.debug("\n{}", event1);
        log.debug("\n{}", event2);
        log.debug("\n{}", jurorPM);
        log.debug("\n{}", jurorMP);

        // Prapun Manyum
        assertThat(event2.getMark(jurorPM), is(8));
        assertThat((double) event2.getOthersAverage(jurorPM), closeTo(7.7, ERROR));
        assertThat((double) event2.getBias(jurorPM), closeTo(0.3, ERROR));

        // Martin Plesch
        assertThat(event2.getMark(jurorMP), is(8));
        assertThat((double) event2.getOthersAverage(jurorMP), closeTo(7.7, ERROR));
        assertThat((double) event2.getBias(jurorMP), closeTo(0.3, ERROR));

        // Chuanyong Li
        assertThat(event2.getMark(jurorCL), is(7));
        assertThat((double) event2.getOthersAverage(jurorCL), closeTo(7.8, ERROR));
        assertThat((double) event2.getBias(jurorCL), closeTo(-0.8, ERROR));

        TreeSet<Juror> jurors = new TreeSet<>(new Juror.BiasComparator());

        jurors.addAll(t.getJurors());

        int marks = 0;
        int i = 0;
        for (Juror juror : jurors) {
            System.out.printf("%d: %s%n", ++i, juror);
            marks += juror.getMarksRecorded();
        }
        // assert that the sum of recorded marks for all jurors equals number of mark rows for the given tournament
        assertThat(marks, is(t.getMarksRecorded()));
    }
}
