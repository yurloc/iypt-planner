package org.iypt.planner.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;

public class PdfCreator {

    public static final String RESULT = "rooms.pdf";
    public static final String RESULT2 = "rounds.pdf";
    private Tournament t;
//    public static File fontFile = new File("arialuni.ttf");

    public void classPdf(Tournament t)
            throws DocumentException, FileNotFoundException, IOException {
        this.t = t;
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        // step 3
        document.open();
        // step 4

        ColumnText column = new ColumnText(writer.getDirectContent());

        for (Group group : t.getGroups()) {
            float height = 0;
            height = addHeaderTable(document, group);
            column.addElement(getTeamRoomTable(t, group));
            column.setSimpleColumn(document.left(), document.bottom(), document.right(), document.top() - 180);
            column.go();
            column.addElement(getJuryRoomTable(t, group));
            column.setSimpleColumn(document.left(), document.bottom(), document.right(), document.top() - 450);
            column.go();
//            
            document.newPage();
        }

        // step 5
        document.close();
    }

    public void roundPdf(Tournament t)
            throws DocumentException, FileNotFoundException, SQLException, IOException {

        this.t = t;
        //step 1
        Document document = new Document(PageSize.A4.rotate());
        //step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT2));
        //step 3
        document.open();
        //step 4

        ColumnText column = new ColumnText(writer.getDirectContent());
        for (Round round : t.getRounds()) {
            float height = addHeaderRoomTable(document, round);
            column.addElement(getRoundTable(t, round));
            column.setSimpleColumn(document.left(), document.bottom(), document.right(), document.top() - height - 40);
            column.go();
            document.newPage();
        }
        //step 5
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

//    public float addTeamTable(Document document, Group group) throws DocumentException {
//        PdfPTable teams = new PdfPTable(1);
//        teams.setWidthPercentage(100);
//        teams.getDefaultCell().setBorderColor(BaseColor.WHITE);
//        Font fTeams = new Font(FontFamily.HELVETICA, 30, Font.NORMAL, BaseColor.BLACK);
//        int count = 1;
//        for (Team team : group.getTeams()) {
//            Phrase pTeams = new Phrase(String.format("Team %d: %s", count, team.getCountry().getName()), fTeams);
//            teams.addCell(pTeams);
//            count++;
//        }
//        document.add(teams);
//        return teams.getTotalHeight();
//    }
//    public float addJurorTable(Document document, Group group) throws DocumentException {
//        PdfPTable juror = new PdfPTable(1);
//        juror.setWidthPercentage(100);
//        juror.getDefaultCell().setBorderColor(BaseColor.WHITE);
//        juror.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
//        Font fJuror;
//        for (Seat s : t.getSeats(group.getJury())) {
//            if (s.isChair()) {
//                fJuror = new Font(FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.RED);
//            } else {
//                fJuror = new Font(FontFamily.HELVETICA, 20, Font.NORMAL, BaseColor.BLACK);
//            }
//            Phrase pJuror = new Phrase(String.format("%s", s.getJuror().fullName()), fJuror);
//            juror.addCell(pJuror);
//        }
//        document.add(juror);
//        return juror.getTotalHeight();
//    }
    public float addHeaderRoomTable(Document document, Round round) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.getDefaultCell().setBorderColor(BaseColor.WHITE);
        Font fGroup = new Font(FontFamily.HELVETICA, 60, Font.BOLD, BaseColor.BLACK);
        Phrase pGroup = new Phrase(String.format("Round #%s", round.getNumber()), fGroup);
        header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(pGroup);
        document.add(header);
        return header.getTotalHeight();
    }

    public PdfPTable getRoundTable(Tournament t, Round r)
            throws DocumentException, IOException {

        PdfPTable table = new PdfPTable(t.getRounds().get(1).getGroups().size());
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setWidthPercentage(105);
        for (Group group : t.getRounds().get(1).getGroups()) {
            table.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);
            Font fRounds = new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
            Phrase pRounds = new Phrase(String.format("Group %s", group.getName()), fRounds);
            table.addCell(pRounds);
        }

        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        String teams = "";
        for (Group g : r.getGroups()) {
            int count = 0;
            for (Team tm : g.getTeams()) {
                if (count == 0) {
                    teams += tm.getCountry();
                } else {
                    teams += ", " + tm.getCountry();
                }
                count++;
            }
            Font fTeams = new Font(FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            Phrase pTeams = new Phrase(String.format("%s", teams), fTeams);
            table.addCell(pTeams);
            teams = "";
        }
        for (Group g : r.getGroups()) {
            Phrase whiteSpace = new Phrase("   ");
            table.addCell(whiteSpace);
        }

        int rows = t.getJuryCapacity();
        for (int i = 0; i < rows; i++) {
            List<String> juryRow = new ArrayList<>();
            for (Group g : r.getGroups()) {
                juryRow.add(t.getSeats(g.getJury()).get(i).getJuror().fullName());
            }
            for (String s : juryRow) {
                Font fJury = new Font(FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
                Phrase pJury = new Phrase(String.format("%s", s), fJury);
                PdfPCell cell = new PdfPCell(pJury);
                cell.setFixedHeight(30f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
        }

        return table;
    }

    public PdfPTable getTeamRoomTable(Tournament t, Group g)
            throws DocumentException, IOException {


        PdfPTable table = new PdfPTable(new float[]{40, 100});
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderColor(BaseColor.WHITE);
        Font fTeams = new Font(FontFamily.HELVETICA, 30, Font.NORMAL, BaseColor.BLACK);
        int count = 1;
        for (Team team : g.getTeams()) {
            Phrase pNum = new Phrase(String.format("Team %d:", count), fTeams);
            table.addCell(pNum);
            Phrase pTeams = new Phrase(String.format("%s", team.getCountry().getName()), fTeams);
            table.addCell(pTeams);
            count++;
        }
        return table;

    }

    public PdfPTable getJuryRoomTable(Tournament t, Group g)
            throws DocumentException, IOException {

        BaseFont unicode = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
//        FontSelector fs = new FontSelector();
//        fs.addFont(new Font(unicode));

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(60);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
//        table.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.getDefaultCell().setBorderColor(BaseColor.WHITE);
//        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        Font fHeader = new Font(FontFamily.HELVETICA, 40, Font.BOLD, BaseColor.BLACK);
        Phrase pHeader = new Phrase("Jury", fHeader);
        table.addCell(pHeader);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BASELINE);
        Font fJury;
        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);

        for (Seat s : t.getSeats(g.getJury())) {
            if (s.isChair()) {
                fJury = new Font(unicode, 25, Font.BOLD, BaseColor.RED);
            } else {
                fJury = new Font(unicode, 25, Font.NORMAL, BaseColor.BLACK);
            }
            Phrase pJuror = new Phrase(String.format("%s", s.getJuror().fullName()), fJury);
            table.addCell(pJuror);
        }
        return table;

    }
}