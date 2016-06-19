package org.iypt.planner.csv;

import com.neovisionaries.i18n.CountryCode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.junit.Test;

import static com.neovisionaries.i18n.CountryCode.CH;
import static com.neovisionaries.i18n.CountryCode.CZ;
import static com.neovisionaries.i18n.CountryCode.DE;
import static com.neovisionaries.i18n.CountryCode.GB;
import static com.neovisionaries.i18n.CountryCode.NZ;
import static com.neovisionaries.i18n.CountryCode.RU;
import static com.neovisionaries.i18n.CountryCode.SK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.iypt.planner.domain.JurorType.INDEPENDENT;
import static org.iypt.planner.domain.JurorType.TEAM_LEADER;

/**
 *
 * @author jlocker
 */
public class CSVTournamentFactoryTest {

    @Test
    public void testAlternativeCSV() throws IOException {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jurors.csv", "schedule2012.csv");
        Tournament t1 = factory.newTournament();
        TournamentUtils tu = new TournamentUtils(t1);

        assertThat(t1.getJurors()).hasSize(82);

        // Martin;Plesch;I;Slovakia;C;Czech Republic
        tu.verifyJuror(tu.getJuror(0, 6, 0), "Martin Plesch", INDEPENDENT, true, SK, CZ);
        // Gavin;Jennings;I;New Zealand;C;United Kingdom
        tu.verifyJuror(tu.getJuror(0, 8, 4), "Gavin Jennings", INDEPENDENT, true, NZ, GB);
        // Ilya;Martchenko;I;Switzerland;Russia;C
        tu.verifyJuror(tu.getJuror(0, 4, 0), "Ilya Martchenko", INDEPENDENT, true, CH, RU);
        // Raimund;Girwidz;I;Germany;1;2;3;4
        tu.verifyJuror(tu.getJuror(4, 7, 1), "Raimund Girwidz", INDEPENDENT, false, DE, 1, 2, 3, 4);
        // Ricky;Regner;T;C;4
        tu.verifyJuror(tu.getJuror(0, 0, 1), "Ricky Regner", TEAM_LEADER, true, new CountryCode[]{});
    }

    @Test
    public void when_new_schedule_read_then_jury_sizes_updated() throws IOException {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jury_data.csv", "schedule2012.csv");
        Tournament tournament = factory.newTournament();

        int maxJurySizes[] = {7, 7, 8, 7, 7};
        for (int i = 0; i < 5; i++) {
            Round round = tournament.getRounds().get(i);
            assertThat(round.getNumber()).as(round.toString()).isEqualTo(i + 1);
            assertThat(round.getJurySize()).as(round.toString()).isEqualTo(6);
            assertThat(round.getMaxJurySize()).as(round.toString()).isEqualTo(maxJurySizes[i]);
        }

        factory.readSchedule(CSVTournamentFactoryTest.class, "schedule2012_variable.csv");
        tournament = factory.newTournament();

        int jurySizes[] = {1, 6, 3, 4, 2};
        for (int i = 0; i < 5; i++) {
            Round round = tournament.getRounds().get(i);
            assertThat(round.getNumber()).as(round.toString()).isEqualTo(i + 1);
            assertThat(round.getJurySize()).as(round.toString()).isEqualTo(jurySizes[i]);
            assertThat(round.getMaxJurySize()).as(round.toString()).isEqualTo(maxJurySizes[i]);
        }
    }

    @Test
    public void reading_bias_jurors_teams_should_not_fail() throws IOException {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readBiasData(CSVTournamentFactoryTest.class, "bias_IYPT2012.csv");
        factory.readJuryData(CSVTournamentFactoryTest.class, "jury_data.csv");
        factory.readTeamData(CSVTournamentFactoryTest.class, "team_data.csv");
        Tournament tournament = factory.newTournament();
        List<Juror> zeroBiasJurors = new ArrayList<>(10);
        for (Juror juror : tournament.getJurors()) {
            if (juror.getBias() == 0) {
                zeroBiasJurors.add(juror);
            }
        }
        assertThat(zeroBiasJurors).hasSize(4);
    }
}
