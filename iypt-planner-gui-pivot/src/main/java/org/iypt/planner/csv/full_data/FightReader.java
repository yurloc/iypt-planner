package org.iypt.planner.csv.full_data;

import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author jlocker
 */
public class FightReader extends AbstractTableReader<FightReader.FightRow> {

    public FightReader() {
        super(FightRow.class);
    }

    @Override
    public CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[]{
            new UniqueHashCode(new ParseInt()),
            new ParseInt(),
            new NotNull(),
            new ParseInt(),
            new ParseInt(),
            new ParseInt(),
            new ParseNull("<NULL>", new ParseInt()),
            new ParseInt(),
            new ParseBool()
        };

        return processors;
    }

    @Override
    public String getTableName() {
        return "Fights";
    }

    public static class FightRow implements HasIntId {

        private int id;
        private int tournament;
        private String group_name;
        private Integer team_1;
        private Integer team_2;
        private Integer team_3;
        private Integer team_4;
        private int round;
        private boolean approved;

        @Override
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getTournament() {
            return tournament;
        }

        public void setTournament(int tournament) {
            this.tournament = tournament;
        }

        public String getGroup_name() {
            return group_name;
        }

        public void setGroup_name(String group_name) {
            this.group_name = group_name;
        }

        public Integer getTeam_1() {
            return team_1;
        }

        public void setTeam_1(Integer team_1) {
            this.team_1 = team_1;
        }

        public Integer getTeam_2() {
            return team_2;
        }

        public void setTeam_2(Integer team_2) {
            this.team_2 = team_2;
        }

        public Integer getTeam_3() {
            return team_3;
        }

        public void setTeam_3(Integer team_3) {
            this.team_3 = team_3;
        }

        public Integer getTeam_4() {
            return team_4;
        }

        public void setTeam_4(Integer team_4) {
            this.team_4 = team_4;
        }

        public int getRound() {
            return round;
        }

        public void setRound(int round) {
            this.round = round;
        }

        public boolean isApproved() {
            return approved;
        }

        public void setApproved(boolean approved) {
            this.approved = approved;
        }
    }
}
