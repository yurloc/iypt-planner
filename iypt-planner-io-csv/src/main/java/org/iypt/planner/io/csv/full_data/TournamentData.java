package org.iypt.planner.io.csv.full_data;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.iypt.planner.io.csv.full_data.model.Fight;
import org.iypt.planner.io.csv.full_data.model.Juror;
import org.iypt.planner.io.csv.full_data.model.Tournament;
import org.iypt.planner.io.csv.full_data.readers.FightReader;
import org.iypt.planner.io.csv.full_data.readers.JurorReader;
import org.iypt.planner.io.csv.full_data.readers.MarkReader;
import org.iypt.planner.io.csv.full_data.readers.PersonReader;
import org.iypt.planner.io.csv.full_data.readers.TournamentReader;
import org.iypt.planner.io.csv.full_data.util.CsvTablesProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlocker
 */
public class TournamentData {

    private static final Logger LOG = LoggerFactory.getLogger(TournamentData.class);
    private final Map<Integer, Tournament> tournaments = new HashMap<>();
    private final Map<Integer, Fight> fights = new HashMap<>();
    private final Map<Integer, Juror> jurors = new HashMap<>();

    public void readData(Reader reader) throws IOException {
        CsvTablesProcessor tables = new CsvTablesProcessor();
        tables.process(reader);

        // read tournaments
        for (TournamentReader.TournamentRow row : new TournamentReader().read(tables).values()) {
            tournaments.put(row.getId(), new Tournament(row));
        }

        // read fights
        for (FightReader.FightRow row : new FightReader().read(tables).values()) {
            Fight fight = new Fight(row, tournaments);
            fights.put(row.getId(), fight);
            fight.getTournament().addFight(fight);
        }

        // read jurors
        for (PersonReader.PersonRow row : new PersonReader().read(tables).values()) {
            Juror juror = new Juror(row, tournaments);
            jurors.put(row.getId(), juror);
            juror.getTournament().addJuror(juror);
        }

        // assign jurors to fights
        for (JurorReader.JurorRow row : new JurorReader().read(tables).values()) {
            Fight fight = fights.get(row.getFight());
            Juror juror = jurors.get(row.getJuror());
            if (!juror.isJuror()) {
                LOG.warn("Person '{}' is not a juror but participates in a fight: {}",
                        juror.getName(), ToStringBuilder.reflectionToString(row, ToStringStyle.SHORT_PREFIX_STYLE));
            }
            fight.assignJuror(row.getJuror_number(), juror);
        }

        // record marks
        for (MarkReader.MarkRow row : new MarkReader().read(tables).values()) {
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
