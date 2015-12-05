package org.iypt.planner.io.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.iypt.planner.api.domain.Assignment;
import org.iypt.planner.api.domain.Group;
import org.iypt.planner.api.domain.Juror;
import org.iypt.planner.api.domain.Role;
import org.iypt.planner.api.domain.Round;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.domain.Tournament;
import org.iypt.planner.api.io.InputSource;
import org.iypt.planner.test.util.RoundFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iypt.planner.test.util.SampleFacts.*;

/**
 *
 * @author jlocker
 */
public class ScheduleWriterTest {

    private static final Logger log = LoggerFactory.getLogger(ScheduleWriterTest.class);
    private static Schedule schedule;

    @BeforeClass
    public static void setUp() throws IOException {
        Round r1 = RoundFactory.createRound(1, gABC, gDEF);
        Round r2 = RoundFactory.createRound(2, gAEI, gBDI);
        r1.setJurySize(3);
        r2.setJurySize(3);
        Tournament tournament = new Tournament(
                Arrays.asList(r1, r2),
                Arrays.asList(jA1, jA2, jB3, jB4, jM5, jM6, jM7)
        );
        int i = 0;
        List<Assignment> assignments = new ArrayList<>();
        for (Round round : tournament.getRounds()) {
            for (Group group : round.getGroups()) {
                for (int j = 0; j < round.getJurySize() + Tournament.NON_VOTING_SEAT_BUFFER; j++) {
                    if (j < round.getJurySize() || round.getNumber() == 1) {
                        Juror juror = tournament.getJurors().get(i);
                        i = (++i) % tournament.getJurors().size();
                        Role role = j == 0 ? Role.CHAIR : j < round.getJurySize() ? Role.VOTING : Role.NON_VOTING;
                        assignments.add(new Assignment(juror, group, role));
                    }
                }
            }
        }
        schedule = new Schedule(tournament, assignments);
    }

    @Test
    public void testDummyTournament() throws IOException {
        StringWriter sw = new StringWriter();
        ScheduleWriter writer = new ScheduleWriter();
        writer.write(sw, schedule);

        log.debug("\n[{}]", sw.toString());

        List<String> actualLines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));

        // read all lines
        for (Round round : schedule.getTournament().getRounds()) {
            for (Group group : round.getGroups()) {
                actualLines.add(br.readLine());
            }
        }

        // all lines have been read
        assertThat(br.readLine()).isNull();

        // check the stored lines
        String[] expectedLines = {
            "1;Group A;1, null;2, null;3, null;(4, null);(5, null)",
            "1;Group B;6, null;7, null;1, null;(2, null);(3, null)",
            "2;Group A;4, null;5, null;6, null",
            "2;Group B;7, null;1, null;2, null"};
        assertThat(actualLines).containsExactly(expectedLines);
    }

    @Test
    public void testInputOutpuMatch() throws IOException {
        // parse IYPT 2012 schedule
        InputSource.ClasspathFactory f = InputSource.newClasspathFactory(ScheduleWriterTest.class, "/org/iypt/planner/io/csv/");
        CsvTournamentImporter imp = new CsvTournamentImporter();
        Tournament t = imp.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jury_data.csv")
        );
        Schedule schedule = imp.loadSchedule(t, f.newInputSource("schedule2012.csv"));

        // write it
        StringWriter sw = new StringWriter();
        ScheduleWriter writer = new ScheduleWriter();
        writer.write(sw, schedule);

        // and read the first line written
        String actual = new BufferedReader(new StringReader(sw.toString())).readLine();

        // read the source file
        InputStreamReader isr = new InputStreamReader(ScheduleWriterTest.class.getResourceAsStream("/org/iypt/planner/io/csv/schedule2012.csv"));
        String expected = new BufferedReader(isr).readLine();

        // verify that what has been written is exactly what has been read (first line only)
        log.info("Comparing input/output:\n[{}]\n[{}]", expected, actual);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testNullJuror() throws IOException {
        Round round = RoundFactory.createRound(1, gABC);
        round.setJurySize(2);
        Tournament t = new Tournament(Arrays.asList(round), Collections.<Juror>emptyList());
        Schedule s = new Schedule(t, Collections.<Assignment>emptyList());
        // writer should not fail to write null jurors
        StringWriter sw = new StringWriter();
        ScheduleWriter writer = new ScheduleWriter();
        writer.write(sw, s);
        log.debug("[{}]", sw.toString());
        assertThat(sw.toString()).isEqualTo("1;Group A\n");
    }
}
