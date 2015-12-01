package org.iypt.planner.pdf;

import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.domain.Tournament;
import org.iypt.planner.api.io.InputSource;
import org.iypt.planner.api.io.TournamentImporter;
import org.iypt.planner.api.pdf.ExportException;
import org.iypt.planner.api.pdf.ExportRequest;
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
        InputSource.ClasspathFactory f = InputSource.newClasspathFactory(PdfTest.class, "/org/iypt/planner/pdf/");
        TournamentImporter imp = ServiceLoader.load(TournamentImporter.class).iterator().next();
        Tournament tournament = imp.loadTournament(
                f.newInputSource("team_data.csv"),
                f.newInputSource("jury_data.csv"));
        schedule = imp.loadSchedule(tournament, f.newInputSource("schedule2012.csv"));
    }

    @Test
    public void test() throws ExportException {
        ExportRequest req = new ExportRequest(schedule, new File("target/pdf"), "test-iypt2012-%t-%s", "yyyyMMdd-HHmmss");
        PdfCreator c = new PdfCreator();
        c.export(req);
    }
}
