package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class Absence {

    private final Juror juror;
    private final int roundNumber;

    public Absence(Juror juror, int roundNumber) {
        this.juror = juror;
        this.roundNumber = roundNumber;
    }

    public Absence(Juror juror, Round round) {
        this.juror = juror;
        this.roundNumber = round.getNumber();
    }

    public Juror getJuror() {
        return juror;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    @Override
    public String toString() {
        return juror + "!" + roundNumber;
    }
}
