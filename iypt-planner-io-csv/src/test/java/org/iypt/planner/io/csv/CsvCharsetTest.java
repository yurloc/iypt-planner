package org.iypt.planner.io.csv;

import java.io.IOException;
import java.nio.charset.Charset;
import org.iypt.planner.api.domain.BiasData;
import org.iypt.planner.api.domain.Juror;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.domain.Tournament;
import org.iypt.planner.api.io.InputSource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jlocker
 */
public class CsvCharsetTest {

    private static final Charset WINDOWS_1250 = Charset.forName("windows-1250");

    @Test
    public void testUtf8() throws IOException {
        InputSource.ClasspathFactory f = InputSource.newClasspathFactory(CsvTournamentImporter.class);
        CsvTournamentImporter imp = new CsvTournamentImporter();
        // claspath resources are expected to be UTF-8 encoded
        Tournament t = imp.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jury_data.csv"));
        BiasData biases = imp.loadBiases(t, f.newInputSource("bias_IYPT2012.csv"));
        Schedule s = imp.loadSchedule(t, f.newInputSource("schedule2012.csv"));
        TournamentUtils tu = new TournamentUtils(s);
        Juror tb = tu.getJuror(0, 8, 3);
        Juror wb = tu.getJuror(0, 7, 5);
        assertThat(tb.getFirstName()).isEqualTo("Tomáš");
        assertThat(tb.getLastName()).isEqualTo("Bzdušek");
        assertThat(wb.getFirstName()).isEqualTo("Władysław");
        assertThat(wb.getLastName()).isEqualTo("Borgieł");
        assertThat(biases.getBias(tb)).isEqualTo(-0.01);
        assertThat(biases.getBias(tb)).isEqualTo(-0.01);
        assertThat(biases.getBias(wb)).isEqualTo(0.81);
    }

    @Test
    public void testWindows1250_correct() throws IOException {
        InputSource.ClasspathFactory f = InputSource.newClasspathFactory(CsvTournamentImporter.class);
        f.setCharset(WINDOWS_1250);
        CsvTournamentImporter imp = new CsvTournamentImporter();
        Tournament t = imp.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jury_data_win.csv"));
        BiasData biases = imp.loadBiases(t, f.newInputSource("bias_IYPT2012_win.csv"));
        Schedule s = imp.loadSchedule(t, f.newInputSource("schedule2012_win.csv"));
        TournamentUtils tu = new TournamentUtils(s);
        Juror tb = tu.getJuror(0, 0, 3);
        assertThat(tb.getFirstName()).isEqualTo("Tomáš");
        assertThat(tb.getLastName()).isEqualTo("Bzdušek");
        assertThat(biases.getBias(tb)).isEqualTo(-0.01);
        Juror wb = tu.getJuror(0, 0, 2);
        assertThat(wb.getFirstName()).isEqualTo("Władysław");
        assertThat(wb.getLastName()).isEqualTo("Borgieł");
        assertThat(biases.getBias(wb)).isEqualTo(0.81);
        Juror fk = tu.getJuror(0, 0, 1);
        assertThat(fk.getFirstName()).isEqualTo("František");
        assertThat(fk.getLastName()).isEqualTo("Kundracik");
        assertThat(biases.getBias(fk)).isEqualTo(-0.18);
        Juror mp = tu.getJuror(0, 0, 0);
        assertThat(mp.getFirstName()).isEqualTo("Martin");
        assertThat(mp.getLastName()).isEqualTo("Plesch");
        assertThat(biases.getBias(mp)).isEqualTo(-0.7);
    }

    @Test
    public void testWindows1250_wrong() throws IOException {
        InputSource.ClasspathFactory f = InputSource.newClasspathFactory(CsvTournamentImporter.class);
        CsvTournamentImporter imp = new CsvTournamentImporter();
        // claspath resources are expected to be UTF-8 encoded
        Tournament t = imp.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jury_data_win.csv"));
        BiasData biases = imp.loadBiases(t, f.newInputSource("bias_IYPT2012_win.csv"));
        Schedule s = imp.loadSchedule(t, f.newInputSource("schedule2012_win.csv"));
        TournamentUtils tu = new TournamentUtils(s);

        // Tomáš Bzdušek
        Juror tb = tu.getJuror(0, 0, 3);
        assertThat(tb.getFirstName()).startsWith("Tom").isNotEqualTo("Tomáš");
        assertThat(tb.getLastName()).startsWith("Bzd").isNotEqualTo("Bzdušek");
        assertThat(biases.getBias(tb)).isEqualTo(-0.01);

        // Władysław Borgieł
        Juror wb = tu.getJuror(0, 0, 2);
        assertThat(wb.getFirstName()).startsWith("W").isNotEqualTo("Władysław");
        assertThat(wb.getLastName()).startsWith("Borgie").isNotEqualTo("Borgieł");
        assertThat(biases.getBias(wb)).isEqualTo(0.81);

        // František Kundracik
        Juror fk = tu.getJuror(0, 0, 1);
        assertThat(fk.getFirstName()).startsWith("Franti").isNotEqualTo("František");
        assertThat(fk.getLastName()).isEqualTo("Kundracik");
        assertThat(biases.getBias(fk)).isEqualTo(-0.18);

        // Martin Plesch
        Juror mp = tu.getJuror(0, 0, 0);
        assertThat(mp.getFirstName()).isEqualTo("Martin");
        assertThat(mp.getLastName()).isEqualTo("Plesch");
        assertThat(biases.getBias(mp)).isEqualTo(-0.7);
    }
}
