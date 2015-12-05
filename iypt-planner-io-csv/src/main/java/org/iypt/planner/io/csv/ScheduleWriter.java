package org.iypt.planner.io.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.iypt.planner.api.domain.Assignment;
import org.iypt.planner.api.domain.Group;
import org.iypt.planner.api.domain.Juror;
import org.iypt.planner.api.domain.Role;
import org.iypt.planner.api.domain.Round;
import org.iypt.planner.api.domain.Schedule;
import org.iypt.planner.api.io.ScheduleExporter;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author jlocker
 */
public class ScheduleWriter implements ScheduleExporter {

    @Override
    public void write(Writer writer, Schedule schedule) throws IOException {
        try (ICsvListWriter listWriter = new CsvListWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {

            // for each assignment
            for (Round round : schedule.getTournament().getRounds()) {
                for (Group group : round.getGroups()) {
                    List<Object> data = new ArrayList<>(round.getJurySize() + 2); // +2 for round and group
                    // append round number and group name
                    data.add(round.getNumber());
                    data.add("Group " + group.getName());
                    // append all jurors
                    for (Assignment assignment : schedule.getAssignments()) {
                        if (assignment.getGroup().equals(group) && assignment.getJuror() != null) {
                            Juror juror = assignment.getJuror();
                            String fmt = "%s, %s";
                            if (assignment.getRole() == Role.NON_VOTING) {
                                fmt = "(%s, %s)";
                            }
                            data.add(String.format(fmt, juror.getLastName(), juror.getFirstName()));
                        }
                    }
                    // write the line
                    listWriter.write(data);
                }
            }
        }
    }
}
