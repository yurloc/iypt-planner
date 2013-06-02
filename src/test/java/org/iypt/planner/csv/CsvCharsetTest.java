package org.iypt.planner.csv;

import java.io.IOException;
import java.nio.charset.Charset;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Tournament;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jlocker
 */
public class CsvCharsetTest {

    private static final Charset WINDOWS_1250 = Charset.forName("windows-1250");
    private static final double ERR = 0.005;

    private Juror getJuror(Tournament t, int round, int group, int seat) {
        return t.getSeats(t.getRounds().get(round).getGroups().get(group).getJury()).get(seat).getJuror();
    }

    @Test
    public void testUtf8() throws IOException {
        CSVTournamentFactory f = new CSVTournamentFactory();
        // claspath resources are read expected to be UTF-8 encoded
        f.readTeamData(CSVTournamentFactory.class, "team_data.csv");
        f.readJuryData(CSVTournamentFactory.class, "jury_data.csv");
        f.readBiasData(CSVTournamentFactory.class, "bias_IYPT2012.csv");
        f.readSchedule(CSVTournamentFactory.class, "schedule2012.csv");
        Tournament t = f.newTournament();
        Juror tb = getJuror(t, 0, 8, 3);
        Juror wb = getJuror(t, 0, 7, 5);
        assertThat(tb.fullName(), is("Tomáš Bzdušek"));
        assertThat(wb.fullName(), is("Władysław Borgieł"));
        assertThat(tb.getBias(), closeTo(-0.01, ERR));
        assertThat(wb.getBias(), closeTo(0.81, ERR));
    }

    @Test
    public void testWindows1250_correct() throws IOException {
        CSVTournamentFactory f = new CSVTournamentFactory();
        f.readTeamData(CSVTournamentFactory.class, "team_data.csv", WINDOWS_1250);
        f.readJuryData(CSVTournamentFactory.class, "jury_data_win.csv", WINDOWS_1250);
        f.readBiasData(CSVTournamentFactory.class, "bias_IYPT2012_win.csv", WINDOWS_1250);
        f.readSchedule(CSVTournamentFactory.class, "schedule2012_win.csv", WINDOWS_1250);
        Tournament t = f.newTournament();
        Juror tb = getJuror(t, 0, 0, 3);
        Juror wb = getJuror(t, 0, 0, 2);
        Juror fk = getJuror(t, 0, 0, 1);
        Juror mp = getJuror(t, 0, 0, 0);
        assertThat(tb.fullName(), is("Tomáš Bzdušek"));
        assertThat(tb.getBias(), closeTo(-0.01, ERR));
        assertThat(wb.fullName(), is("Władysław Borgieł"));
        assertThat(wb.getBias(), closeTo(0.81, ERR));
        assertThat(fk.fullName(), is("František Kundracik"));
        assertThat(fk.getBias(), closeTo(-0.18, ERR));
        assertThat(mp.fullName(), is("Martin Plesch"));
        assertThat(mp.getBias(), closeTo(-0.7, ERR));
    }

    @Test
    public void testWindows1250_wrong() throws IOException {
        CSVTournamentFactory f = new CSVTournamentFactory();
        // claspath resources are read expected to be UTF-8 encoded
        f.readTeamData(CSVTournamentFactory.class, "team_data.csv");
        f.readJuryData(CSVTournamentFactory.class, "jury_data_win.csv");
        f.readBiasData(CSVTournamentFactory.class, "bias_IYPT2012_win.csv");
        f.readSchedule(CSVTournamentFactory.class, "schedule2012_win.csv");
        Tournament t = f.newTournament();
        Juror tb = getJuror(t, 0, 0, 3);
        Juror wb = getJuror(t, 0, 0, 2);
        Juror fk = getJuror(t, 0, 0, 1);
        Juror mp = getJuror(t, 0, 0, 0);

        // Tomáš Bzdušek
        assertThat(tb.getFirstName(), startsWith("Tom"));
        assertThat(tb.getLastName(), startsWith("Bzd"));
        assertThat(tb.fullName(), not("Tomáš Bzdušek"));
        assertThat(tb.getBias(), closeTo(-0.01, ERR));

        // Władysław Borgieł
        assertThat(wb.getFirstName(), startsWith("W"));
        assertThat(wb.getLastName(), startsWith("Borgie"));
        assertThat(wb.fullName(), not("Władysław Borgieł"));
        assertThat(wb.getBias(), closeTo(0.81, ERR));

        // František Kundracik
        assertThat(fk.getFirstName(), startsWith("Franti"));
        assertThat(fk.getLastName(), is("Kundracik"));
        assertThat(fk.fullName(), not("František Kundracik"));
        assertThat(fk.getBias(), closeTo(-0.18, ERR));

        // Martin Plesch
        assertThat(mp.fullName(), is("Martin Plesch"));
        assertThat(mp.getBias(), closeTo(-0.7, ERR));
    }
}
