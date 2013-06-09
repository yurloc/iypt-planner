package org.iypt.planner.csv;

import java.io.IOException;
import org.iypt.planner.domain.Tournament;
import org.junit.Test;

import static com.neovisionaries.i18n.CountryCode.*;
import static org.fest.assertions.api.Assertions.*;
import static org.iypt.planner.domain.JurorType.*;

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
    }
}
