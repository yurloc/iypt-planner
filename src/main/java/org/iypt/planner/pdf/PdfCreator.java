package org.iypt.planner.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;

public class PdfCreator {
 
    public static final String RESULT = "testung.pdf";

 
   
    public void createPdf(Tournament t)
        throws DocumentException, FileNotFoundException {
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer
            = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        // step 3
        document.open();
        // step 4
        ColumnText column = new ColumnText(writer.getDirectContent());
        // COlumn definition
        float[][] x = {
                { document.left(), document.left() + 380 },
                { document.right() - 380, document.right() }
            };
        
        /////////////////////////////////////////////////////////////////////////////
        
        // Loop over the festival days
        //for (Date day : days) {
            // add content to the column
        for(Group group : t.getGroups()) {
            int count = 0;
            float height = 0;
            int status = ColumnText.START_COLUMN;
            // render the column as long as it has content
            while (ColumnText.hasMoreText(status)) {
            	// add the top-level header to each new page
                if (count == 0) {
                    height = addHeaderTable(
                        document, group);
                }
                // set the dimensions of the current column
                column.setSimpleColumn(
                    x[count][0], document.bottom(),
                    x[count][1], document.top() - height - 10);
                // render as much content as possible
                status = column.go();  
            }
            addGroupTable(document, group);
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
        System.out.printf("header: %s, %s%n", group.getRound(), group);
        return header.getTotalHeight();
    }
    
    public float addGroupTable(Document document, Group group) throws DocumentException {
        PdfPTable teams = new PdfPTable(1);
        teams.setWidthPercentage(100);
        teams.getDefaultCell().setBorderColor(BaseColor.WHITE);
        Font fTeams = new Font(FontFamily.HELVETICA, 40, Font.NORMAL, BaseColor.BLACK);
        System.out.printf("group: %s%n", group);
        for(Team team : group.getTeams()) {
            System.out.printf("team: %s%n", team);
            Phrase pTeams = new Phrase(String.format("%s asasd", team.getCountry().getName(), fTeams));
            teams.addCell(pTeams);
        }
        document.add(teams);
        return teams.getTotalHeight();
    }
}