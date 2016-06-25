package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class JurorLoad {

    public static final double INFINITE_LOAD_VALUE = 2.0;
    private final Juror juror;
    private final boolean chair;
    private final double load;
    private final double delta;
    private final double cost;
    private final boolean excessive;

    public JurorLoad(Juror juror, boolean chair, Number seats, int rounds, Number absences, double optimal) {
        this(juror, chair, seats.intValue(), rounds, absences.intValue(), optimal);
    }

    public JurorLoad(Juror juror, boolean chair, int seats, int rounds, int absences, double optimal) {
        this.juror = juror;
        this.chair = chair;
        double allowed = 0;
        int roundsAvailable = rounds - absences;
        if (!juror.isExperienced() && roundsAvailable > 0) {
            roundsAvailable--;
        }
        if (roundsAvailable == 0) {
            // avoid division by zero
            if (seats == 0) {
                load = 1; // utilized at 100% even though not used
                allowed = 1 - optimal; // will be never excessive
            } else {
                load = INFINITE_LOAD_VALUE;
            }
        } else {
            load = ((double) seats) / roundsAvailable;
            allowed = 1.0 / roundsAvailable;
        }
        delta = load - optimal;
        if (seats == 0 && roundsAvailable > 0) {
            // juror that is available for at least 1 round and not used must be always penalized
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

    public boolean isChair() {
        return chair;
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
        return String.format("%s's %sload: %.2f(%+.2f)", juror, chair ? "chair " : "", load, delta);
    }
}
