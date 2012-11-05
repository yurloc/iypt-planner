package org.iypt.planner.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.iypt.planner.csv.FightReader.FightRow;
import org.iypt.planner.csv.JurorReader.JurorRow;
import org.iypt.planner.csv.MarkReader.MarkRow;
import org.iypt.planner.csv.PersonReader.PersonRow;
import org.iypt.planner.csv.TournamentReader.TournamentRow;

/**
 *
 * @author jlocker
 */
public class TournamentData {

    private final Map<Integer, Tournament> tournaments = new HashMap<>();
    private final Map<Integer, Fight> fights = new HashMap<>();
    private final Map<Integer, Juror> jurors = new HashMap<>();

    public void readData(Reader reader) throws IOException {
        CsvTablesProcessor tables = new CsvTablesProcessor();
        tables.process(reader);

        // read tournaments
        for (TournamentRow row : new TournamentReader().read(tables).values()) {
            tournaments.put(row.getId(), new Tournament(row));
        }

        // read fights
        for (FightRow row : new FightReader().read(tables).values()) {
            Fight fight = new Fight(row, tournaments);
            fights.put(row.getId(), fight);
            fight.getTournament().addFight(fight);
        }

        // read jurors
        for (PersonRow row : new PersonReader().read(tables).values()) {
            // don't care about other participants, only jurors
            if (row.isJuror()) {
                Juror juror = new Juror(row, tournaments);
                jurors.put(row.getId(), juror);
                juror.getTournament().addJuror(juror);
            }
        }

        // assign jurors to fights
        for (JurorRow row : new JurorReader().read(tables).values()) {
            Fight fight = fights.get(row.getFight());
            Juror juror = jurors.get(row.getJuror());
            fight.assignJuror(row.getJuror_number(), juror);
        }

        // record marks
        for (MarkRow row : new MarkReader().read(tables).values()) {
            Fight fight = fights.get(row.getFight());
            fight.recordMark(row);
        }
    }

    Tournament getTournament(int id) {
        return tournaments.get(id);
    }

    Fight getFight(int id) {
        return fights.get(id);
    }

    Juror getJuror(int id) {
        return jurors.get(id);
    }

    public Collection<Tournament> getTournaments() {
        return tournaments.values();
    }
}
