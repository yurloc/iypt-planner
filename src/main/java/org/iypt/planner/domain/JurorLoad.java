package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class JurorLoad {

    private Juror juror;
    private double load;
    private double delta;
    private boolean excessive;

    public JurorLoad(Juror juror, Number seats, int rounds, Number dayOffs, double optimal) {
        this.juror = juror;
        load = ((double) seats.intValue()) / (rounds - dayOffs.intValue());
        delta = load - optimal;
        double allowed = 1.0 / (rounds - dayOffs.intValue());
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
