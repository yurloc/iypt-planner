package org.iypt.planner.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.RoundFactory;
import org.junit.Test;

import static org.iypt.planner.domain.util.SampleFacts.*;

/**
 *
 * @author Falen
 */
public class PdfTest {

    @Test
    public void test() throws DocumentException, FileNotFoundException {
        String filename = "test.pdf";

        // step 1
        Document document = new Document();
        // step 2
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        // step 3
        document.open();
        // step 4
        document.add(createFirstTable());
        // step 5
        document.close();
    }

    private void assignJurors(Tournament t, Juror... jurors) {
        Iterator<Seat> it = t.getSeats().iterator();
        for (int i = 0; i < jurors.length; i++) {
            it.next().setJuror(jurors[i]);
        }
    }

    public PdfPTable createFirstTable() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF);
        t.addRounds(r1);
        t.setJuryCapacity(2);
        assignJurors(t, jM1, jN2, jN1, jM2);
        // a table with three columns
        PdfPTable table = new PdfPTable(7);
        // the cell object
        PdfPCell cell;
        // we add a cell with colspan 3
        cell = new PdfPCell(new Phrase("Round 1"));
        cell.setColspan(5);
        table.addCell(cell);
        // now we add a cell with rowspan 2
        cell = new PdfPCell(new Phrase(""));
        cell.setRowspan(6);
        table.addCell(cell);
        // we add the four remaining cells with addCell()
        for (Seat s : t.getSeats()) {
            table.addCell(s.getJuror().toString());
        }
        return table;

        /*for (Seat s : t.getSeats()) {
         document.add(new Paragraph(s.getJuror().toString()));
         }*/
    }
}
