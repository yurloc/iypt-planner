package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class JurorLoad {

    public static final double INFINITE_LOAD_VALUE = 2.0;
    private final Juror juror;
    private final double load;
    private final double delta;
    private final double cost;
    private final boolean excessive;

    public JurorLoad(Juror juror, Number seats, int rounds, Number absences, double optimal) {
        this(juror, seats.intValue(), rounds, absences.intValue(), optimal);
    }

    public JurorLoad(Juror juror, int seats, int rounds, int absences, double optimal) {
        this.juror = juror;
        double allowed = 0;
        if (rounds == absences) {
            // avoid division by zero
            if (seats == 0) {
                load = 0;
                allowed = optimal; // will be never excessive
            } else {
                load = INFINITE_LOAD_VALUE;
            }
        } else {
            load = ((double) seats) / (rounds - absences);
            allowed = 1.0 / (rounds - absences);
        }
        delta = load - optimal;
        if (seats == 0) {
            cost = 1.0;
            excessive = true;
        } else {
            cost = Math.abs(delta);
            excessive = Math.abs(delta) > allowed;
        }
    }

    public Juror getJuror() {
        return juror;
    }

    public double getLoad() {
        return load;
    }

    public double getDelta() {
        return delta;
    }

    public double getCost() {
        return cost;
    }

    public boolean isExcessive() {
        return excessive;
    }

    @Override
    public String toString() {
        return String.format("%s's load: %.2f(%+.2f)", juror, load, delta);
    }
}
