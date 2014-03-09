package org.iypt.planner.csv.full_data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;
import org.assertj.core.data.Offset;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 *
 * @author jlocker
 */
public class TournamentDataTest {

    private static final Logger log = LoggerFactory.getLogger(TournamentDataTest.class);
    private static TournamentData data;
    private static final Offset<Float> OFFSET = offset(0.005f);

    @BeforeClass
    public static void setUp() throws IOException {
        data = new TournamentData();
        data.readData(new InputStreamReader(ReadersTest.class.getResourceAsStream("full_data.csv")));
    }

    @Test
    public void testData() {
        Tournament t = data.getTournament(9);
        assertThat(t.getName()).isEqualTo("IYPT2012");

        // check a few rows to see the data has been read correctly
        Juror jurorMP = data.getJuror(9420);
        Juror jurorPM = data.getJuror(9437);
        Juror jurorCL = data.getJuror(9419);
        assertThat(jurorMP.getName()).isEqualTo("Martin Plesch");
        assertThat(jurorPM.getName()).isEqualTo("Prapun Manyum");
        assertThat(jurorCL.getName()).isEqualTo("Chuanyong Li");

        Fight fight1A = data.getFight(1376);
        Fight fight1G = data.getFight(1382);
        Fight fightFinal = data.getFight(1453);

        // all these fights are from IYPT2012
        assertThat(fight1A.getTournament()).isSameAs(t);
        assertThat(fight1G.getTournament()).isSameAs(t);
        assertThat(fightFinal.getTournament()).isSameAs(t);

        // and are reachable from it
        assertThat(t.getFights()).contains(fight1A);
        assertThat(t.getFights()).contains(fight1G);
        assertThat(t.getFights()).contains(fightFinal);

        // check the right jurors are assigned to final fight
        assertThat(fightFinal.getJurors()).contains(jurorMP, jurorPM, jurorCL);

        t.calculate();

        JudgingEvent event1 = fight1G.getStage(3).getJudgingEvent(1);
        JudgingEvent event2 = fightFinal.getStage(3).getJudgingEvent(3);

        log.debug("\n{}", event1);
        log.debug("\n{}", event2);
        log.debug("\n{}", jurorPM);
        log.debug("\n{}", jurorMP);

        // Prapun Manyum
        assertThat(event2.getMark(jurorPM)).isEqualTo(8);
        assertThat(event2.getOthersAverage(jurorPM)).isEqualTo(7.7f, OFFSET);
        assertThat(event2.getBias(jurorPM)).isEqualTo(0.3f, OFFSET);

        // Martin Plesch
        assertThat(event2.getMark(jurorMP)).isEqualTo(8);
        assertThat(event2.getOthersAverage(jurorMP)).isEqualTo(7.7f, OFFSET);
        assertThat(event2.getBias(jurorMP)).isEqualTo(0.3f, OFFSET);

        // Chuanyong Li
        assertThat(event2.getMark(jurorCL)).isEqualTo(7);
        assertThat(event2.getOthersAverage(jurorCL)).isEqualTo(7.8f, OFFSET);
        assertThat(event2.getBias(jurorCL)).isEqualTo(-0.8f, OFFSET);

        TreeSet<Juror> jurors = new TreeSet<>(new Juror.BiasComparator());

        jurors.addAll(t.getJurors());

        int marks = 0;
        int i = 0;
        for (Juror juror : jurors) {
            System.out.printf("%d: %s%n", ++i, juror);
            marks += juror.getMarksRecorded();
        }
        // assert that the sum of recorded marks for all jurors equals number of mark rows for the given tournament
        assertThat(marks).isEqualTo(t.getMarksRecorded());
    }
}
