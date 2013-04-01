package org.iypt.planner.csv.full_data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jlocker
 */
public class BiasWriterTest {

    private static final Logger log = LoggerFactory.getLogger(BiasWriterTest.class);
    private static TournamentData data;
    private static final int head = 2;
    private static final int tail = 4;

    @BeforeClass
    public static void setUp() throws IOException {
        data = new TournamentData();
        data.readData(new InputStreamReader(ReadersTest.class.getResourceAsStream("full_data.csv")));
    }

    @Test
    public void testData() throws IOException {
        Tournament t = data.getTournament(9);
        t.calculate();

        TreeSet<Juror> jurors = new TreeSet<>(new Juror.BiasComparator());
        jurors.addAll(t.getJurors());
        // +1 for the header line
        int totalLines = jurors.size() + 1;

        StringWriter sw = new StringWriter();
        BiasWriter writer = new BiasWriter(jurors);
        writer.write(sw);

        log.debug(sw.toString());

        List<String> actualLines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));

        // store some lines from head
        for (int i = 0; i < head; i++) {
            actualLines.add(br.readLine());
        }

        // skip the middle
        for (int i = 0; i < totalLines - head - tail; i++) {
            br.readLine();
        }

        // store some lines from tail
        for (int i = 0; i < tail; i++) {
            actualLines.add(br.readLine());
        }

        // all lines have been read
        assertThat(br.readLine(), nullValue());

        // check the stored lines
        String[] expectedLines = {
            "given_name,last_name,bias",
            "Florian,Ostermaier,-0.8111",
            "Władysław,Borgieł,0.8056",
            "Michael,Gierling,0.8889",
            "Nien,Cheng-Hsun,0",
            "Uno,Uno,0"};
        assertThat(actualLines, contains(expectedLines));
    }
}
