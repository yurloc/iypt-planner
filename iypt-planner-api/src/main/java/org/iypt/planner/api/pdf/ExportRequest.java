package org.iypt.planner.api.pdf;

import java.io.File;
import java.util.Date;
import org.iypt.planner.api.domain.Schedule;

public class ExportRequest {

    private final Schedule schedule;
    private final File outputDir;
    private final String formatString;
    private final String dateFormat;
    private final Date date;

    public ExportRequest(Schedule schedule, File outputDir, String formatString, String dateFormat) {
        this.schedule = schedule;
        this.outputDir = outputDir;
        this.formatString = formatString;
        this.dateFormat = dateFormat;
        this.date = new Date();
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public String getFormatString() {
        return formatString;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }
}
