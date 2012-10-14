package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class JurorLoad {

    public static final double INFINITE_LOAD_VALUE = 2.0;
    private Juror juror;
    private double load;
    private double delta;
    private boolean excessive;

    public JurorLoad(Juror juror, Number seats, int rounds, Number dayOffs, double optimal) {
        this(juror, seats.intValue(), rounds, dayOffs.intValue(), optimal);
    }

    public JurorLoad(Juror juror, int seats, int rounds, int dayOffs, double optimal) {
        this.juror = juror;
        double allowed = 0;
        if (rounds == dayOffs) {
            // avoid division by zero
            if (seats == 0) {
                load = 0;
                allowed = optimal; // will be never excessive
            } else {
                load = INFINITE_LOAD_VALUE;
            }
        } else {
            load = ((double) seats) / (rounds - dayOffs);
            allowed = 1.0 / (rounds - dayOffs);
        }
        delta = load - optimal;
        excessive = Math.abs(delta) > allowed;
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

    public boolean isExcessive() {
        return excessive;
    }

    @Override
    public String toString() {
        return String.format("%s's load: %.2f(%+.2f)", juror, load, delta);
    }
}
