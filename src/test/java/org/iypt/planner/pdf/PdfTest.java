package org.iypt.planner.pdf;

import com.itextpdf.text.DocumentException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.domain.Tournament;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Falen
 */
public class PdfTest {

    private static Tournament tournament;

    @BeforeClass
    public static void setupTournament() throws IOException {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jury_data.csv", "schedule2012.csv");
        tournament = factory.newTournament();
        tournament.setJuryCapacity(6);
    }

    @Test
    public void test() throws DocumentException, FileNotFoundException, SQLException, IOException {
        PdfCreator c = new PdfCreator();

        c.classPdf(tournament);
        c.roundPdf(tournament);
    }
}
