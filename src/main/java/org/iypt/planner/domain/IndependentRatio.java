package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class IndependentRatio {

    private final Jury jury;
    private final double optimal;
    private final int count;
    private final int delta;

    public IndependentRatio(Jury jury, Number independentCount) {
        this.jury = jury;
        count = independentCount.intValue();
        optimal = jury.getGroup().getRound().getOptimalIndependentCount();
        delta = (int) (count - optimal);
    }

    public int getDelta() {
        return delta;
    }

    @Override
    public String toString() {
        return String.format("%d(%+d) in %s(%.2f)", count, delta, jury, optimal);
    }
}
