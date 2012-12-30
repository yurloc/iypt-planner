package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class Lock {

    private final Juror juror;
    private final Jury jury;
    private final int position;

    public Lock(Juror juror, Jury jury, int position) {
        this.juror = juror;
        this.jury = jury;
        this.position = position;
    }

    public boolean matches(Seat seat) {
        return seat.getJury() == jury && seat.getPosition() == position;
    }

    public Juror getJuror() {
        return juror;
    }

    public Jury getJury() {
        return jury;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("%s locked on seat %d in %s", juror, position, jury);
    }
}
