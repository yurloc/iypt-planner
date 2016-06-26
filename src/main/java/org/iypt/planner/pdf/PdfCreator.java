package org.iypt.planner.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.neovisionaries.i18n.CountryCode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.iypt.planner.domain.Absence;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.CountryCodeIO;

public class PdfCreator {

    private final Tournament t;
    private final BaseFont bf;
    private final Date date = new Date();
    private String filePrefix = "";
    private File outputDir = null;

    public PdfCreator(Tournament tournament) {
        this.t = tournament;
        bf = FontFactory.getFont("/ttf/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED).getBaseFont();
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public void setFilePrefix(String prefix) {
        this.filePrefix = prefix;
    }

    private File getOutputFile(String id) {
        String fileName = String.format("%s%s.pdf", filePrefix, id);
        return outputDir == null ? new File(fileName) : new File(outputDir, fileName);
    }

    public void printRooms() throws DocumentException, IOException {
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(getOutputFile("rooms")));
        writer.setPageEvent(new TimestampFooter(date));
        // step 3
        document.open();
        // step 4
        ColumnText column = new ColumnText(writer.getDirectContent());
        for (Group group : t.getGroups()) {
            // group header
            PdfPTable header = getRoomHeaderTable(group);
            document.add(header);

            // teams
            PdfPTable teams = getRoomTeamTable(group);
            column.addElement(teams);
            column.setSimpleColumn(document.left(), document.bottom(), document.right(),
                    document.top() - header.getTotalHeight() - 40);
            column.go();

            // jury
            column.addElement(getRoomJuryTable(t, group));
            column.setSimpleColumn(document.left(), document.bottom(), document.right(),
                    document.top() - header.getTotalHeight() - teams.getTotalHeight() - 80);
            column.go();
            document.newPage();
        }
        // step 5
        document.close();
    }

    public void printRounds() throws DocumentException, IOException {
        //step 1
        Document document = new Document(PageSize.A4.rotate());
        //step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(getOutputFile("rounds")));
        writer.setPageEvent(new TimestampFooter(date));
        //step 3
        document.open();
        //step 4
        ColumnText column = new ColumnText(writer.getDirectContent());
        for (Round round : t.getRounds()) {
            PdfPTable header = getRoundHeaderTable(round);
            document.add(header);
            column.addElement(getRoundTable(t, round));
            column.setSimpleColumn(document.left(), document.bottom(), document.right(),
                    document.top() - header.getTotalHeight() - 40);
            column.go();
            document.newPage();
        }
        //step 5
        document.close();
    }

    public void printIdleJurors() throws DocumentException, IOException {
        //step 1
        Document document = new Document(PageSize.A4);
        //step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(getOutputFile("idle")));
        writer.setPageEvent(new TimestampFooter(date));
        //step 3
        document.open();
        //step 4
        ColumnText column = new ColumnText(writer.getDirectContent());
        for (Round round : t.getRounds()) {
            PdfPTable header = getRoundHeaderTable(round);
            document.add(header);
            column.addElement(getRoundIdleJurorsTable(t, round));
            column.setSimpleColumn(document.left(), document.bottom(), document.right(),
                    document.top() - header.getTotalHeight() - 40);
            column.go();
            document.newPage();
        }
        //step 5
        document.close();
    }

    private PdfPTable getRoundHeaderTable(Round round) {
        Phrase pGroup = new Phrase(round.toString(), new Font(bf, 60));

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.getDefaultCell().setBorderColor(BaseColor.WHITE);
        header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(pGroup);

        return header;
    }

    private PdfPTable getRoundTable(Tournament t, Round r) {
        Font fRounds = new Font(bf, 10, Font.BOLD);
        Font fTeams = new Font(bf, 10);
        Font fJury = new Font(bf, 10);
        Font fObs = new Font(bf, 10, Font.NORMAL, BaseColor.GRAY);

        PdfPTable table = new PdfPTable(r.getGroups().size());
        table.setWidthPercentage(100);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        // Header with group names
        for (Group group : r.getGroups()) {
            PdfPCell cGroup = new PdfPCell(new Phrase(group.getName(), fRounds));
            cGroup.setPadding(10);
            cGroup.setHorizontalAlignment(Element.ALIGN_CENTER);
            cGroup.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cGroup);
        }

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
        int rows = t.getSeats(r.getGroups().get(0).getJury()).size();
        for (int i = 0; i < rows; i++) {
            List<String> juryRow = new ArrayList<>();
            for (Group g : r.getGroups()) {
                Seat s = t.getSeats(g.getJury()).get(i);
                Juror juror = s.getJuror();
                if (juror != null) {
                    if (s.isVoting()) {
                        juryRow.add(juror.fullName());
                    } else {
                        juryRow.add(String.format("[%s]", juror.fullName()));
                    }
                } else {
                    juryRow.add("");
                }
            }
            for (String s : juryRow) {
                Phrase pJury = new Phrase(s, i < r.getJurySize() ? fJury : fObs);
                PdfPCell cell = new PdfPCell(pJury);
                cell.setFixedHeight(30f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
        }

        return table;
    }

    private Element getRoundIdleJurorsTable(Tournament t, Round round) {
        Font fJuror = new Font(bf, 24);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderColor(BaseColor.WHITE);

        // header
        table.addCell(getHeaderCell("Not scheduled jurors"));

        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BASELINE);
        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        ArrayList<Juror> idle = new ArrayList<Juror>(t.getJurors());
        for (Group g : round.getGroups()) {
            for (Seat s : t.getSeats(g.getJury())) {
                idle.remove(s.getJuror());
            }
        }
        for (Juror juror : idle) {
            boolean absent = false;
            for (Absence a : t.getAbsences(juror)) {
                if (a.getRound().equals(round)) {
                    absent = true;
                }
            }
            if (!absent) {
                table.addCell(new Phrase(juror.fullName(), fJuror));
            }
        }
        return table;
    }

    private PdfPCell getHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(bf, 28, Font.BOLD)));
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

    private PdfPTable getRoomHeaderTable(Group group) {
        Font fGroup = new Font(bf, 72, Font.BOLD);
        Font fRound = new Font(bf, 20);
        Phrase pGroup = new Phrase(group.getName(), fGroup);
        Phrase pRound = new Phrase(group.getRound().toString(), fRound);

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.getDefaultCell().setBorderColor(BaseColor.WHITE);
        header.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(pGroup);
        header.addCell(pRound);

        return header;
    }

    private PdfPTable getRoomTeamTable(Group g) {
        Font fTeams = new Font(bf, 24);

        PdfPTable table = new PdfPTable(new float[]{1, 4});
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderColor(BaseColor.WHITE);

        // header cell
        table.addCell(getHeaderCell("T E A M S"));

        int count = 1;
        for (Team team : g.getTeams()) {
            table.addCell(getNumberCell(count, fTeams));
            table.addCell(new Phrase(CountryCodeIO.getShortName(team.getCountry()), fTeams));
            count++;
        }
        return table;
    }

    private PdfPTable getRoomJuryTable(Tournament t, Group g) {
        Font fChair = new Font(bf, 24, Font.BOLD);
        Font fJuror = new Font(bf, 24);
        Font fNonVoting = new Font(bf, 24, Font.NORMAL, BaseColor.GRAY);

        PdfPTable table = new PdfPTable(new float[]{1, 4});
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderColor(BaseColor.WHITE);

        // header
        table.addCell(getHeaderCell("J U R Y"));

        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BASELINE);
        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        int count = 1;
        for (Seat s : t.getSeats(g.getJury())) {
            String title = null;
            Font font = null;
            if (s.isChair()) {
                title = String.format("%s (chair)", s.getJuror().fullName());
                font = fChair;
            } else if (s.isVoting()) {
                title = s.getJuror().fullName();
                font = fJuror;
            } else if (s.getJuror() != null) {
                title = String.format("%s (obs.)", s.getJuror().fullName());
                font = fNonVoting;
            }
            if (title != null) {
                table.addCell(getNumberCell(count++, font));
                table.addCell(new Phrase(title, font));
            }
        }
        return table;
    }
}
