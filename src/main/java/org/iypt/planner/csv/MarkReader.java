package org.iypt.planner.csv;

import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author jlocker
 */
public class MarkReader extends AbstractTableReader<MarkReader.MarkRow> {

    public MarkReader() {
        super(MarkRow.class);
    }

    @Override
    public CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[]{
            new UniqueHashCode(new ParseInt()),
            new ParseInt(),
            new ParseInt(),
            new ParseInt(),
            new ParseInt(),
            new ParseInt()
        };

        return processors;
    }

    @Override
    public String getTableName() {
        return "Marks";
    }

    public static class MarkRow implements HasIntId {

        private int id;
        private int fight;
        private int stage;
        private int juror_number;
        private int role;
        private int mark;

        @Override
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getFight() {
            return fight;
        }

        public void setFight(int fight) {
            this.fight = fight;
        }

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public int getJuror_number() {
            return juror_number;
        }

        public void setJuror_number(int juror_number) {
            this.juror_number = juror_number;
        }

        public int getRole() {
            return role;
        }

        public void setRole(int role) {
            this.role = role;
        }

        public int getMark() {
            return mark;
        }

        public void setMark(int mark) {
            this.mark = mark;
        }
    }
}
