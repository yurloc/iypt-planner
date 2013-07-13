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
import com.neovisionaries.i18n.CountryCode;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;

public class PdfCreator {

    public static final String RESULT = "rooms.pdf";
    public static final String RESULT2 = "rounds.pdf";

    public void printRooms(Tournament t) throws DocumentException, IOException {
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
            column.addElement(getTeamRoomTable(group));
            column.setSimpleColumn(document.left(), document.bottom(), document.right(), document.top() - 180);
            column.go();
            column.addElement(getJuryRoomTable(t, group));
            column.setSimpleColumn(document.left(), document.bottom(), document.right(), document.top() - 450);
            column.go();
            document.newPage();
        }

        // step 5
        document.close();
    }

    public void printRounds(Tournament t) throws DocumentException, IOException {
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

    private float addHeaderTable(Document document, Group group) throws DocumentException {
        Font fGroup = new Font(FontFamily.HELVETICA, 80, Font.BOLD, BaseColor.BLACK);
        Font fRound = new Font(FontFamily.HELVETICA, 20, Font.NORMAL, BaseColor.BLACK);
        Phrase pGroup = new Phrase(String.format("GROUP %s", group.getName()), fGroup);
        Phrase pRound = new Phrase(group.getRound().toString(), fRound);

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.getDefaultCell().setBorderColor(BaseColor.WHITE);
        header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(pGroup);
        header.addCell(pRound);

        document.add(header);
        return header.getTotalHeight();
    }

    private float addHeaderRoomTable(Document document, Round round) throws DocumentException {
        Font fGroup = new Font(FontFamily.HELVETICA, 60, Font.BOLD, BaseColor.BLACK);
        Phrase pGroup = new Phrase(round.toString(), fGroup);

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.getDefaultCell().setBorderColor(BaseColor.WHITE);
        header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(pGroup);

        document.add(header);
        return header.getTotalHeight();
    }

    private PdfPTable getRoundTable(Tournament t, Round r) {
        Font fRounds = new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
        Font fTeams = new Font(FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        Font fJury = new Font(FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

        PdfPTable table = new PdfPTable(t.getRounds().get(1).getGroups().size());
        table.setWidthPercentage(100);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);

        // Header with group names
        for (Group group : t.getRounds().get(1).getGroups()) {
            Phrase pRounds = new Phrase(String.format("Group %s", group.getName()), fRounds);
            table.addCell(pRounds);
        }
        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);

        // Teams in each group
        for (Group g : r.getGroups()) {
            List<CountryCode> cList = new ArrayList<>(4);
            for (Team tm : g.getTeams()) {
                cList.add(tm.getCountry());
            }
            Phrase pTeams = new Phrase(StringUtils.join(cList, ", "), fTeams);
            table.addCell(pTeams);
        }

        // Empty row
        for (Group g : r.getGroups()) {
            Phrase whiteSpace = new Phrase("   ");
            table.addCell(whiteSpace);
        }

        // Jurors
        int rows = t.getJuryCapacity();
        for (int i = 0; i < rows; i++) {
            List<String> juryRow = new ArrayList<>();
            for (Group g : r.getGroups()) {
                juryRow.add(t.getSeats(g.getJury()).get(i).getJuror().fullName());
            }
            for (String s : juryRow) {
                Phrase pJury = new Phrase(s, fJury);
                PdfPCell cell = new PdfPCell(pJury);
                cell.setFixedHeight(30f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
        }

        return table;
    }

    private PdfPCell getHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(FontFamily.HELVETICA, 30, Font.BOLD, BaseColor.BLACK)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(10);
        cell.setColspan(2);
        cell.setBorderColor(BaseColor.WHITE);
        return cell;
    }

    private PdfPCell getNumberCell(int count, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(count), font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setPaddingRight(20);
        return cell;
    }

    private PdfPTable getTeamRoomTable(Group g) {
        Font fTeams = new Font(FontFamily.HELVETICA, 30, Font.NORMAL, BaseColor.BLACK);

        PdfPTable table = new PdfPTable(new float[]{1, 4});
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderColor(BaseColor.WHITE);

        // header cell
        table.addCell(getHeaderCell("T E A M S"));

        int count = 1;
        for (Team team : g.getTeams()) {
            table.addCell(getNumberCell(count, fTeams));
            table.addCell(new Phrase(team.getCountry().getName(), fTeams));
            count++;
        }
        return table;
    }

    private PdfPTable getJuryRoomTable(Tournament t, Group g) throws DocumentException, IOException {
        BaseFont unicode = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
//        FontSelector fs = new FontSelector();
//        fs.addFont(new Font(unicode));
        Font fChair = new Font(unicode, 30, Font.BOLD, BaseColor.BLACK);
        Font fJuror = new Font(unicode, 30, Font.NORMAL, BaseColor.BLACK);

        PdfPTable table = new PdfPTable(new float[]{1, 4});
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderColor(BaseColor.WHITE);

        // header
        table.addCell(getHeaderCell("J U R Y"));

        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BASELINE);
        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        int count = 1;
        for (Seat s : t.getSeats(g.getJury())) {
            table.addCell(getNumberCell(count++, fJuror));
            Phrase pJuror;
            if (s.isChair()) {
                pJuror = new Phrase(String.format("%s (chair)", s.getJuror().fullName()), fChair);
            } else {
                pJuror = new Phrase(s.getJuror().fullName(), fJuror);
            }
            table.addCell(pJuror);
        }
        return table;
    }
}