package org.iypt.planner.io.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.iypt.planner.api.domain.BiasData;
import org.iypt.planner.api.domain.Juror;
import org.iypt.planner.api.domain.Round;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.domain.Tournament;
import org.iypt.planner.api.io.InputSource;
import org.junit.Test;

import static com.neovisionaries.i18n.CountryCode.CH;
import static com.neovisionaries.i18n.CountryCode.CZ;
import static com.neovisionaries.i18n.CountryCode.DE;
import static com.neovisionaries.i18n.CountryCode.GB;
import static com.neovisionaries.i18n.CountryCode.NZ;
import static com.neovisionaries.i18n.CountryCode.RU;
import static com.neovisionaries.i18n.CountryCode.SK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.iypt.planner.api.domain.JurorType.INDEPENDENT;

public class CsvTournamentImporterTest {

    private final InputSource.ClasspathFactory f = InputSource.newClasspathFactory(
            CsvTournamentImporterTest.class,
            "/org/iypt/planner/io/csv/"
    );

    @Test
    public void testAlternativeCSV() throws IOException {
        CsvTournamentImporter imp = new CsvTournamentImporter();
        Tournament t = imp.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jurors.csv"));
        Schedule s = imp.loadSchedule(t, f.newInputSource("schedule2012.csv"));
        TournamentUtils tu = new TournamentUtils(s);

        assertThat(s.getTournament().getJurors()).hasSize(82);

        // Martin;Plesch;I;Slovakia;C;Czech Republic
        tu.verifyJuror(tu.getJuror(0, 6, 0), "Martin", "Plesch", INDEPENDENT, true, SK, CZ);
        // Gavin;Jennings;I;New Zealand;C;United Kingdom
        tu.verifyJuror(tu.getJuror(0, 8, 4), "Gavin", "Jennings", INDEPENDENT, true, NZ, GB);
        // Ilya;Martchenko;I;Switzerland;Russia;C
        tu.verifyJuror(tu.getJuror(0, 4, 0), "Ilya", "Martchenko", INDEPENDENT, true, CH, RU);
        // Raimund;Girwidz;I;Germany;1;2;3;4
        tu.verifyJuror(tu.getJuror(4, 7, 1), "Raimund", "Girwidz", INDEPENDENT, false, DE, 1, 2, 3, 4);
    }

    @Test
    public void when_new_schedule_read_then_jury_sizes_updated() throws IOException {
        CsvTournamentImporter imp = new CsvTournamentImporter();
        Tournament t = imp.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jury_data.csv"));
        Schedule s = imp.loadSchedule(t, f.newInputSource("schedule2012.csv"));
        Tournament tournament = s.getTournament();

        // TODO fix this or remove it
//        int maxJurySizes[] = {7, 7, 8, 7, 7};
        for (int i = 0; i < 5; i++) {
            Round round = tournament.getRounds().get(i);
            assertThat(round.getNumber()).as(round.toString()).isEqualTo(i + 1);
            assertThat(round.getJurySize()).as(round.toString()).isEqualTo(6);
//            assertThat(round.getMaxJurySize()).as(round.toString()).isEqualTo(maxJurySizes[i]);
        }

        imp.loadSchedule(tournament, f.newInputSource("schedule2012_variable.csv"));

        int jurySizes[] = {1, 6, 3, 4, 2};
        for (int i = 0; i < 5; i++) {
            Round round = tournament.getRounds().get(i);
            assertThat(round.getNumber()).as(round.toString()).isEqualTo(i + 1);
            assertThat(round.getJurySize()).as(round.toString()).isEqualTo(jurySizes[i]);
//            assertThat(round.getMaxJurySize()).as(round.toString()).isEqualTo(maxJurySizes[i]);
        }
    }

    @Test
    public void reading_bias_jurors_teams_should_not_fail() throws IOException {
        CsvTournamentImporter imp = new CsvTournamentImporter();
        Tournament tournament = imp.loadTournament(f.newInputSource("team_data.csv"), f.newInputSource("jury_data.csv"));
        BiasData biases = imp.loadBiases(tournament, f.newInputSource("bias_IYPT2012.csv"));
        List<Juror> zeroBiasJurors = new ArrayList<>(10);
        for (Juror juror : tournament.getJurors()) {
            if (biases.getBias(juror) == 0) {
                zeroBiasJurors.add(juror);
            }
        }
        assertThat(zeroBiasJurors).hasSize(4);
    }
}
