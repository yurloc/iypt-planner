package org.iypt.planner.pdf;

import com.itextpdf.text.DocumentException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.RoundFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.iypt.planner.domain.util.SampleFacts.*;

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
    public void test() throws DocumentException, FileNotFoundException{
        /*Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF, tG, tH, tI);
        Round r2 = RoundFactory.createRound(2, tA, tB, tC, tD, tE, tF, tG, tH, tI);
        Round r3 = RoundFactory.createRound(3, tA, tB, tC, tD, tE, tF, tG, tH, tI);
        Round r4 = RoundFactory.createRound(4, tA, tB, tC, tD, tE, tF, tG, tH, tI);

        t.addRounds(r1);
        t.addRounds(r2);
        t.addRounds(r3);
        t.addRounds(r4);
        t.setJuryCapacity(1);
        assignJurors(t, jM1, jN2, jN1, jM2);*/
        
        
        
        PdfCreator c = new PdfCreator();
        c.createPdf(tournament);
    }


    private void assignJurors(Tournament t, Juror... jurors) {
        Iterator<Seat> it = t.getSeats().iterator();
        for (int i = 0; i < jurors.length; i++) {
            it.next().setJuror(jurors[i]);
        }
    }
}
