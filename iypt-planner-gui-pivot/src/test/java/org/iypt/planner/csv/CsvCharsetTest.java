package org.iypt.planner.csv;

import java.io.IOException;
import java.nio.charset.Charset;
import org.iypt.planner.domain.Juror;
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
        CSVTournamentFactory f = new CSVTournamentFactory();
        // claspath resources are read expected to be UTF-8 encoded
        f.readTeamData(CSVTournamentFactory.class, "team_data.csv");
        f.readJuryData(CSVTournamentFactory.class, "jury_data.csv");
        f.readBiasData(CSVTournamentFactory.class, "bias_IYPT2012.csv");
        f.readSchedule(CSVTournamentFactory.class, "schedule2012.csv");
        TournamentUtils tu = new TournamentUtils(f.newTournament());
        Juror tb = tu.getJuror(0, 8, 3);
        Juror wb = tu.getJuror(0, 7, 5);
        assertThat(tb.fullName()).isEqualTo("Tomáš Bzdušek");
        assertThat(wb.fullName()).isEqualTo("Władysław Borgieł");
        assertThat(tb.getBias()).isEqualTo(-0.01);
        assertThat(tb.getBias()).isEqualTo(-0.01);
        assertThat(wb.getBias()).isEqualTo(0.81);
    }

    @Test
    public void testWindows1250_correct() throws IOException {
        CSVTournamentFactory f = new CSVTournamentFactory();
        f.readTeamData(CSVTournamentFactory.class, "team_data.csv", WINDOWS_1250);
        f.readJuryData(CSVTournamentFactory.class, "jury_data_win.csv", WINDOWS_1250);
        f.readBiasData(CSVTournamentFactory.class, "bias_IYPT2012_win.csv", WINDOWS_1250);
        f.readSchedule(CSVTournamentFactory.class, "schedule2012_win.csv", WINDOWS_1250);
        TournamentUtils tu = new TournamentUtils(f.newTournament());
        Juror tb = tu.getJuror(0, 0, 3);
        Juror wb = tu.getJuror(0, 0, 2);
        Juror fk = tu.getJuror(0, 0, 1);
        Juror mp = tu.getJuror(0, 0, 0);
        assertThat(tb.fullName()).isEqualTo("Tomáš Bzdušek");
        assertThat(tb.getBias()).isEqualTo(-0.01);
        assertThat(wb.fullName()).isEqualTo("Władysław Borgieł");
        assertThat(wb.getBias()).isEqualTo(0.81);
        assertThat(fk.fullName()).isEqualTo("František Kundracik");
        assertThat(fk.getBias()).isEqualTo(-0.18);
        assertThat(mp.fullName()).isEqualTo("Martin Plesch");
        assertThat(mp.getBias()).isEqualTo(-0.7);
    }

    @Test
    public void testWindows1250_wrong() throws IOException {
        CSVTournamentFactory f = new CSVTournamentFactory();
        // claspath resources are read expected to be UTF-8 encoded
        f.readTeamData(CSVTournamentFactory.class, "team_data.csv");
        f.readJuryData(CSVTournamentFactory.class, "jury_data_win.csv");
        f.readBiasData(CSVTournamentFactory.class, "bias_IYPT2012_win.csv");
        f.readSchedule(CSVTournamentFactory.class, "schedule2012_win.csv");
        TournamentUtils tu = new TournamentUtils(f.newTournament());
        Juror tb = tu.getJuror(0, 0, 3);
        Juror wb = tu.getJuror(0, 0, 2);
        Juror fk = tu.getJuror(0, 0, 1);
        Juror mp = tu.getJuror(0, 0, 0);

        // Tomáš Bzdušek
        assertThat(tb.getFirstName()).startsWith("Tom");
        assertThat(tb.getLastName()).startsWith("Bzd");
        assertThat(tb.fullName()).isNotEqualTo("Tomáš Bzdušek");
        assertThat(tb.getBias()).isEqualTo(-0.01);

        // Władysław Borgieł
        assertThat(wb.getFirstName()).startsWith("W");
        assertThat(wb.getLastName()).startsWith("Borgie");
        assertThat(wb.fullName()).isNotEqualTo("Władysław Borgieł");
        assertThat(wb.getBias()).isEqualTo(0.81);

        // František Kundracik
        assertThat(fk.getFirstName()).startsWith("Franti");
        assertThat(fk.getLastName()).isEqualTo("Kundracik");
        assertThat(fk.fullName()).isNotEqualTo("František Kundracik");
        assertThat(fk.getBias()).isEqualTo(-0.18);

        // Martin Plesch
        assertThat(mp.fullName()).isEqualTo("Martin Plesch");
        assertThat(mp.getBias()).isEqualTo(-0.7);
    }
}
