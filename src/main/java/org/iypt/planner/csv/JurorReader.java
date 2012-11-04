package org.iypt.planner.csv;

import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author jlocker
 */
public class JurorReader extends AbstractTableReader<JurorReader.JurorRow> {

    public JurorReader() {
        super(JurorRow.class);
    }

    @Override
    public CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[]{
            new UniqueHashCode(new ParseInt()),
            new ParseInt(),
            new ParseInt(),
            new ParseInt()
        };

        return processors;
    }

    @Override
    public String getTableName() {
        return "Jurors";
    }

    public static class JurorRow implements HasIntId {

        private int id;
        private int fight;
        private int juror;
        private int juror_number;

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

        public int getJuror() {
            return juror;
        }

        public void setJuror(int juror) {
            this.juror = juror;
        }

        public int getJuror_number() {
            return juror_number;
        }

        public void setJuror_number(int juror_number) {
            this.juror_number = juror_number;
        }
    }
}
