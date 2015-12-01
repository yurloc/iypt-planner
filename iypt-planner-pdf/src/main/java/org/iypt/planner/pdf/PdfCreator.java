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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.iypt.planner.api.domain.Assignment;
import org.iypt.planner.api.domain.Group;
import org.iypt.planner.api.domain.Juror;
import org.iypt.planner.api.domain.Role;
import org.iypt.planner.api.domain.Round;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.domain.Team;
import org.iypt.planner.api.domain.Tournament;
import org.iypt.planner.api.pdf.ExportException;
import org.iypt.planner.api.pdf.ExportRequest;
import org.iypt.planner.api.pdf.PdfExporter;
import org.iypt.planner.api.util.CountryCodeIO;

public class PdfCreator implements PdfExporter {

    private final BaseFont bf = FontFactory.getFont(
            "/ttf/DejaVuSans.ttf",
            BaseFont.IDENTITY_H,
            BaseFont.EMBEDDED).getBaseFont();

    @Override
    public void export(ExportRequest request) throws ExportException {
        try {
            printRooms(request);
            printRounds(request);
        } catch (DocumentException | IOException ex) {
            throw new ExportException(ex);
        }
    }

    private File getOutputFile(ExportRequest request, String id) {
        File outputDir = request.getOutputDir();
        String format = request.getFormatString();

        String fileName = format.replaceAll("%s", id);

        if (format.contains("%t")) {
            fileName = fileName.replaceAll("%t", new SimpleDateFormat(request.getDateFormat()).format(request.getDate()));
        }

        fileName += ".pdf";

        if (outputDir == null) {
            return new File(fileName);
        }

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                Logger.getLogger(PdfCreator.class.getName()).log(
                        Level.INFO,
                        "Can't create output directory '{0}'",
                        outputDir.getAbsolutePath());
            }
        }

        return new File(outputDir, fileName);
    }

    public void printRooms(ExportRequest request) throws DocumentException, IOException {
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(getOutputFile(request, "rooms")));
        // step 3
        document.open();
        // step 4
        ColumnText column = new ColumnText(writer.getDirectContent());
        Schedule schedule = request.getSchedule();
        for (Round round : schedule.getTournament().getRounds()) {
            for (Group group : round.getGroups()) {
                // group header
                PdfPTable header = getRoomHeaderTable(round, group);
                document.add(header);

                // teams
                PdfPTable teams = getRoomTeamTable(group);
                column.addElement(teams);
                column.setSimpleColumn(document.left(), document.bottom(), document.right(),
                        document.top() - header.getTotalHeight() - 40);
                column.go();

                // jury
                column.addElement(getRoomJuryTable(schedule, group));
                column.setSimpleColumn(document.left(), document.bottom(), document.right(),
                        document.top() - header.getTotalHeight() - teams.getTotalHeight() - 80);
                column.go();
                document.newPage();
            }
        }
        // step 5
        document.close();
    }

    private void printRounds(ExportRequest request) throws DocumentException, IOException {
        //step 1
        Document document = new Document(PageSize.A4.rotate());
        //step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(getOutputFile(request, "rounds")));
        //step 3
        document.open();
        //step 4
        ColumnText column = new ColumnText(writer.getDirectContent());
        Schedule schedule = request.getSchedule();
        for (Round round : schedule.getTournament().getRounds()) {
            PdfPTable header = getRoundHeaderTable(round);
            document.add(header);
            column.addElement(getRoundTable(schedule, round));
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

    private PdfPTable getRoundTable(Schedule sched, Round r) {
        Font fRounds = new Font(bf, 10, Font.BOLD);
        Font fTeams = new Font(bf, 10);
        Font fJury = new Font(bf, 10);
        Font fObs = new Font(bf, 10, Font.NORMAL, BaseColor.GRAY);

        Tournament t = sched.getTournament();
        PdfPTable table = new PdfPTable(t.getRounds().get(1).getGroups().size());
        table.setWidthPercentage(100);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        // Header with group names
        for (Group group : t.getRounds().get(1).getGroups()) {
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
        Map<Group, List<Assignment>> map = new HashMap<>();
        for (Group group : r.getGroups()) {
            List<Assignment> jury = new ArrayList<>();
            map.put(group, jury);
            for (Assignment assignment : sched.getAssignments()) {
                if (assignment.getGroup().equals(group)) {
                    jury.add(assignment);
                }
            }
        }
        int rows = r.getJurySize() + Tournament.NON_VOTING_SEAT_BUFFER;
        for (int i = 0; i < rows; i++) {
            List<String> juryRow = new ArrayList<>();
            for (Group g : r.getGroups()) {
                List<Assignment> jury = map.get(g);
                if (jury.size() > i) {
                    Assignment a = jury.get(i);
                    Juror juror = a.getJuror();
                    if (a.getRole() == Role.VOTING) {
                        juryRow.add(String.format("%s %s", juror.getFirstName(), juror.getLastName()));
                    } else {
                        juryRow.add(String.format("[%s %s]", juror.getFirstName(), juror.getLastName()));
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

    private PdfPTable getRoomHeaderTable(Round round, Group group) {
        Font fGroup = new Font(bf, 72, Font.BOLD);
        Font fRound = new Font(bf, 20);
        Phrase pGroup = new Phrase(group.getName(), fGroup);
        Phrase pRound = new Phrase(round.toString(), fRound);

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

    private PdfPTable getRoomJuryTable(Schedule sched, Group g) {
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
        for (Assignment a : sched.getAssignments()) {
            if (a.getGroup().equals(g)) {
                String name = String.format("%s %s", a.getJuror().getFirstName(), a.getJuror().getLastName());
                String title = null;
                Font font = null;
                switch (a.getRole()) {
                    case CHAIR:
                        title = String.format("%s (chair)", name);
                        font = fChair;
                        break;
                    case VOTING:
                        title = name;
                        font = fJuror;
                        break;
                    case NON_VOTING:
                        title = String.format("%s (obs.)", name);
                        font = fNonVoting;
                        break;
                    default:
                        throw new AssertionError();
                }
                if (title != null) {
                    table.addCell(getNumberCell(count++, font));
                    table.addCell(new Phrase(title, font));
                }
            }
        }
        return table;
    }
}
