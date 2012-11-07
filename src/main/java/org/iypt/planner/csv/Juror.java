package org.iypt.planner.csv;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.iypt.planner.csv.PersonReader.PersonRow;

/**
 *
 * @author jlocker
 */
public class Juror {

    private final PersonRow row;
    private final Tournament tournament;
    private final List<Bias> biases = new ArrayList<>();
    private float accumulatedBias = 0;

    Juror(PersonRow row, Map<Integer, Tournament> tournaments) {
        this.row = row;
        this.tournament = tournaments.get(row.getTournament());
        if (this.tournament == null) {
            throw new IllegalArgumentException("Cannot find tournament with id " + row.getTournament());
        }
    }

    Tournament getTournament() {
        return tournament;
    }

    void addBias(Bias bias) {
        biases.add(bias);
        accumulatedBias += bias.getValue();
    }

    public float getAverageBias() {
        return accumulatedBias / biases.size();
    }

    public int getMarksRecorded() {
        return biases.size();
    }

    public String getName() {
        return row.getFull_name();
    }

    public String getGivenName() {
        return row.getGiven_name();
    }

    public String getLastName() {
        return row.getLast_name();
    }

    @Override
    public String toString() {
        return String.format("%s {averageBias=%+.2f, marksRecorded=%d}", getName(), getAverageBias(), biases.size());
    }

    public static class BiasComparator implements Comparator<Juror> {

        @Override
        public int compare(Juror o1, Juror o2) {
            int compare = Float.compare(o1.getAverageBias(), o2.getAverageBias());
            if (compare != 0) {
                return compare;
            }
            return o1.getLastName().compareTo(o2.getLastName());
        }
    }
}
