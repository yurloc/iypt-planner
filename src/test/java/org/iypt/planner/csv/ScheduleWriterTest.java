package org.iypt.planner.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.RoundFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iypt.planner.domain.util.SampleFacts.*;

/**
 *
 * @author jlocker
 */
public class ScheduleWriterTest {

    @BeforeClass
    public static void setUp() throws IOException {
        Round r1 = RoundFactory.createRound(1, gABC, gDEF);
        Round r2 = RoundFactory.createRound(2, gABC, gDEF);
        tournament = new Tournament();
        tournament.setJuryCapacity(3);
        tournament.addRounds(r1, r2);
        List<Juror> jurors = Arrays.asList(jA1, jA2, jB2, jB2, jC1, jC2, jD1, jD2);
        tournament.addJurors(jurors);
        int i = 0;
        for (Round round : tournament.getRounds()) {
            for (Group group : round.getGroups()) {
                for (Seat seat : tournament.getSeats(group.getJury())) {
                    seat.setJuror(jurors.get(i));
                    i = (++i) % jurors.size();
                }
            }
        }
    }
    private static final Logger log = LoggerFactory.getLogger(ScheduleWriterTest.class);
    private static Tournament tournament;

    @Test
    public void testDummyTournament() throws IOException {
        StringWriter sw = new StringWriter();
        ScheduleWriter writer = new ScheduleWriter(tournament);
        writer.write(sw);

        log.debug("\n[{}]", sw.toString());

        List<String> actualLines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));

        // read all lines
        int lines = tournament.getGroups().size();
        for (int i = 0; i < lines; i++) {
            actualLines.add(br.readLine());
        }

        // all lines have been read
        assertThat(br.readLine()).isNull();

        // check the stored lines
        String[] expectedLines = {
            "1;Group A;1, null;2, null;1, null",
            "1;Group B;2, null;2, null;2, null",
            "2;Group A;1, null;2, null;1, null",
            "2;Group B;2, null;2, null;2, null"};
        assertThat(actualLines).containsExactly(expectedLines);
    }

    @Test
    public void testInputOutpuMatch() throws IOException {
        // parse IYPT 2012 schedule
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jury_data.csv", "schedule2012.csv");
        Tournament t = factory.newTournament();

        // write it
        StringWriter sw = new StringWriter();
        ScheduleWriter writer = new ScheduleWriter(t);
        writer.write(sw);

        // and read the first line written
        String actual = new BufferedReader(new StringReader(sw.toString())).readLine();

        // read the source file
        InputStreamReader isr = new InputStreamReader(ScheduleWriterTest.class.getResourceAsStream("/org/iypt/planner/csv/schedule2012.csv"));
        String expected = new BufferedReader(isr).readLine();

        // verify that what has been written is exactly what has been read (first line only)
        log.info("Comparing input/output:\n[{}]\n[{}]", expected, actual);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testNullJuror() throws IOException {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, gABC));
        // writer should not fail to write null jurors
        StringWriter sw = new StringWriter();
        ScheduleWriter writer = new ScheduleWriter(t);
        writer.write(sw);
        log.debug("[{}]", sw.toString());
        assertThat(sw.toString()).isEqualTo("1;Group A;null, null;null, null\n");
    }
}
