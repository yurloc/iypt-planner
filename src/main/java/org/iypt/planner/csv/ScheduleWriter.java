package org.iypt.planner.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author jlocker
 */
public class ScheduleWriter {

    public ScheduleWriter(Tournament tournament) {
        this.tournament = tournament;
    }

    private final Tournament tournament;

    public void write(Writer writer) throws IOException {
        try (ICsvListWriter listWriter = new CsvListWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {

            // for each group
            for (Round round : tournament.getRounds()) {
                for (Group group : round.getGroups()) {
                    List<Object> data = new ArrayList<>(tournament.getJuryCapacity() + 2);
                    // append round number and group name
                    data.add(round.getNumber());
                    data.add("Group " + group.getName());
                    // append all jurors
                    for (Seat seat : tournament.getSeats(group.getJury())) {
                        Juror juror = seat.getJuror();
                        if (juror == null) {
                            juror = Juror.NULL;
                        }
                        data.add(String.format("%s, %s", juror.getLastName(), juror.getFirstName()));
                    }
                    // write the line
                    listWriter.write(data);
                }
            }
        }
    }
}
