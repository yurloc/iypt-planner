package org.iypt.planner.opta.drools.domain;

/**
 *
 * @author jlocker
 */
public class Absence {

    private final Juror juror;
    private final Round round;

    public Absence(Juror juror, Round round) {
        this.juror = juror;
        this.round = round;
    }

    public Juror getJuror() {
        return juror;
    }

    public Round getRound() {
        return round;
    }

    @Override
    public String toString() {
        return juror + "!" + round.getNumber();
    }
}
