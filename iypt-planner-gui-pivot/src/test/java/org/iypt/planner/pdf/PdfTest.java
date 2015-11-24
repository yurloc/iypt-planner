package org.iypt.planner.pdf;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.util.ServiceLoader;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.io.InputSource;
import org.iypt.planner.api.io.TournamentImporter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Falen
 */
public class PdfTest {

    private static Schedule schedule;

    @BeforeClass
    public static void setupTournament() throws IOException {
        InputSource.ClasspathFactory f = InputSource.newClasspathFactory(PdfTest.class, "/org/iypt/planner/csv/");
        TournamentImporter imp = ServiceLoader.load(TournamentImporter.class).iterator().next();
        org.iypt.planner.api.domain.Tournament t = imp.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jury_data.csv"));
        schedule = imp.loadSchedule(t, f.newInputSource("schedule2012.csv"));
    }

    @Test
    public void test() throws DocumentException, IOException {
        PdfCreator c = new PdfCreator(schedule);
        c.setFilePrefix("iypt2012_");

        c.printRooms();
        c.printRounds();
    }
}
