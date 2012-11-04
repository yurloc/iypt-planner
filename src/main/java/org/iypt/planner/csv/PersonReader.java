package org.iypt.planner.csv;

import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author jlocker
 */
public class PersonReader extends AbstractTableReader<PersonReader.Person> {

    public PersonReader() {
        super(Person.class);
    }

    @Override
    public CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[]{
            new UniqueHashCode(new ParseInt()),
            new ParseInt(),
            new NotNull(),
            new NotNull(),
            new NotNull(),
            new ParseBool()
        };

        return processors;
    }

    @Override
    public String getTableName() {
        return "Persons";
    }

    public static class Person implements HasIntId {

        private int id;
        private int tournament;
        private String given_name;
        private String last_name;
        private String full_name;
        private boolean juror;

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

        public String getGiven_name() {
            return given_name;
        }

        public void setGiven_name(String given_name) {
            this.given_name = given_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getFull_name() {
            return full_name;
        }

        public void setFull_name(String full_name) {
            this.full_name = full_name;
        }

        public boolean isJuror() {
            return juror;
        }

        public void setJuror(boolean juror) {
            this.juror = juror;
        }
    }
}
