package org.iypt.planner.csv.full_data;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author jlocker
 */
public class TournamentReader extends AbstractTableReader<TournamentReader.TournamentRow> {

    public TournamentReader() {
        super(TournamentRow.class);
    }

    @Override
    public CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[]{
            new UniqueHashCode(new ParseInt()),
            new NotNull(),
            new Optional()
        };

        return processors;
    }

    @Override
    public String getTableName() {
        return "Tournaments";
    }

    public static class TournamentRow implements HasIntId {

        private int id;
        private String tournament_name;
        private String rule_set;

        @Override
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTournament_name() {
            return tournament_name;
        }

        public void setTournament_name(String tournament_name) {
            this.tournament_name = tournament_name;
        }

        public String getRule_set() {
            return rule_set;
        }

        public void setRule_set(String rule_set) {
            this.rule_set = rule_set;
        }
    }
}
