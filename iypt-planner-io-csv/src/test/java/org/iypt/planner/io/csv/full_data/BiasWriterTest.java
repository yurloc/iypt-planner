package org.iypt.planner.io.csv.full_data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.iypt.planner.io.csv.full_data.model.Juror;
import org.iypt.planner.io.csv.full_data.model.Tournament;
import org.iypt.planner.io.csv.full_data.writers.BiasWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jlocker
 */
public class BiasWriterTest {

    private static final Logger log = LoggerFactory.getLogger(BiasWriterTest.class);
    private static final NumberFormat fmt = new DecimalFormat("#.####");
    private static TournamentData data;

    @BeforeClass
    public static void setUp() throws IOException {
        data = new TournamentData();
        data.readData(new InputStreamReader(ReadersTest.class.getResourceAsStream("full_data.csv"), StandardCharsets.UTF_8));
    }

    @Test
    public void testData() throws IOException {
        Tournament t = data.getTournament(9);
        t.calculate();

        TreeSet<Juror> jurors = new TreeSet<>(new Juror.BiasComparator());
        for (Juror juror : t.getJurors()) {
            if (juror.isJuror()) {
                jurors.add(juror);
            }
        }
        // +1 for the header line
        int totalLines = jurors.size() + 1;

        StringWriter sw = new StringWriter();
        BiasWriter writer = new BiasWriter(jurors);
        writer.write(sw);

        log.debug(sw.toString());

        List<String> actualLines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new StringReader(sw.toString()));

        int[] lines = {0, 1, 19, 35, 79, 80, 81, 82};
        int next = 0;

        // store lines of interest
        for (int i = 0; i < totalLines; i++) {
            String line = br.readLine();
            if (i == lines[next]) {
                actualLines.add(line);
                next++;
            }
        }

        // all lines have been read
        assertThat(br.readLine()).isNull();

        // check the stored lines
        String[] expectedLines = {
            "given_name;last_name;bias",
            createLine("Florian", "Ostermaier", -0.8111),
            createLine("František", "Kundracik", -0.1833),
            createLine("Tomáš", "Bzdušek", -0.0111),
            createLine("Władysław", "Borgieł", 0.8056),
            createLine("Michael", "Gierling", 0.8889),
            createLine("Nien", "Cheng-Hsun", 0),
            createLine("Uno", "Uno", 0)};
        assertThat(actualLines).containsExactly(expectedLines);
    }

    private String createLine(String f, String l, double b) {
        return String.format("%s;%s;%s", f, l, fmt.format(b));
    }
}
