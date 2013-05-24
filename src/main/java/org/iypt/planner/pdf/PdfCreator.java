package org.iypt.planner.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;

public class PdfCreator {

    public static final String RESULT = "testung.pdf";
    private Tournament t;

    public void createPdf(Tournament t)
            throws DocumentException, FileNotFoundException {
        this.t = t;
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        // step 3
        document.open();
        // step 4

        for (Group group : t.getGroups()) {
            float height = 0;
            height = addHeaderTable(document, group);
            addGroupTable(document, group);
            addJurorTable(document, group);
            document.newPage();
        }

        // step 5
        document.close();
    }

    public float addHeaderTable(Document document, Group group) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.getDefaultCell().setBorderColor(BaseColor.WHITE);
        Font fGroup = new Font(FontFamily.HELVETICA, 80, Font.BOLD, BaseColor.BLACK);
        Phrase pGroup = new Phrase(String.format("GROUP %s", group.getName()), fGroup);
        header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(pGroup);
        Font fRound = new Font(FontFamily.HELVETICA, 20, Font.NORMAL, BaseColor.BLACK);
        Phrase pRound = new Phrase(String.format("%s", group.getRound()), fRound);
        header.addCell(pRound);
        document.add(header);
        return header.getTotalHeight();
    }

    public float addGroupTable(Document document, Group group) throws DocumentException {
        PdfPTable teams = new PdfPTable(1);
        teams.setWidthPercentage(100);
        teams.getDefaultCell().setBorderColor(BaseColor.WHITE);
//        teams.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        Font fTeams = new Font(FontFamily.HELVETICA, 30, Font.NORMAL, BaseColor.BLACK);
        int count = 1;
        for (Team team : group.getTeams()) {
            Phrase pTeams = new Phrase(String.format("Team %d: %s", count, team.getCountry().getName()), fTeams);
            teams.addCell(pTeams);
            count++;
        }
        document.add(teams);
        return teams.getTotalHeight();
    }

    public float addJurorTable(Document document, Group group) throws DocumentException {
        PdfPTable juror = new PdfPTable(1);
        juror.setWidthPercentage(100);
        juror.getDefaultCell().setBorderColor(BaseColor.WHITE);
        juror.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        Font fJuror;
        for (Seat s : t.getSeats(group.getJury())) {
            if (s.isChair()) {
                fJuror = new Font(FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.RED);
            } else {
                fJuror = new Font(FontFamily.HELVETICA, 20, Font.NORMAL, BaseColor.BLACK);
            }
            Phrase pJuror = new Phrase(String.format("%s", s.getJuror().fullName()), fJuror);
            juror.addCell(pJuror);
        }
        document.add(juror);
        return juror.getTotalHeight();
    }
}