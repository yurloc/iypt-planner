package org.iypt.planner.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.TreeSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jlocker
 */
public class BiasWriterTest {

    private static final Logger log = LoggerFactory.getLogger(BiasWriterTest.class);
    private static TournamentData data;

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

        StringWriter sw = new StringWriter();
        BiasWriter writer = new BiasWriter(jurors);
        writer.write(sw);

        log.debug(sw.toString());

        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
        assertThat(br.readLine(), is("given_name,last_name,bias"));
        assertThat(br.readLine(), is("Florian,Ostermaier,-0.81"));

        // skip the middle
        for (int i = 0; i < 79; i++) {
            br.readLine();
        }

        // check the last two lines
        assertThat(br.readLine(), is("Nien,Cheng-Hsun,0"));
        assertThat(br.readLine(), is("Uno,Uno,0"));
    }
}
