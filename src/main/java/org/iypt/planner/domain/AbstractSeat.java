package org.iypt.planner.domain;

public abstract class AbstractSeat implements Seat {

    // fixed
    protected final Jury jury;
    protected final int position;
    // planning variable
    protected Juror juror;

    public AbstractSeat(Jury jury, int position, Juror juror) {
        this.jury = jury;
        this.position = position;
        this.juror = juror;
    }

    public abstract Seat clone();

    /**
     * Get the jury this seat belongs to.
     *
     * @return the jury this seat belongs to
     */
    @Override
    public Jury getJury() {
        return jury;
    }

    /**
     * Get the exact position of this seat in the jury.
     *
     * @return number of the seat
     */
    @Override
    public int getPosition() {
        return position;
    }

    /**
     * Assign a juror to this seat.
     *
     * @param juror juror that will occupy this seat
     */
    @Override
    public void setJuror(Juror juror) {
        this.juror = juror;
    }

    /**
     * Determine if a juror is assigned to this seat.
     *
     * @return True if a juror is assigned to this seat.
     */
    @Override
    public boolean isOccupied() {
        return juror != null && juror != Juror.NULL;
    }

    /**
     * Determine if this is the jury chair's seat.
     *
     * @return True if this seat must be occupied by a jury chair
     */
    @Override
    public boolean isChair() {
        return position == 0;
    }

    @Override
    public String toString() {
        return String.format("[Seat %s:%d]-[%s]", jury.coords(), position + 1, juror);
    }

    @Override
    public int compareTo(Seat other) {
        if (jury.equals(other.getJury())) {
            return position - other.getPosition();
        }
        return jury.getGroup().compareTo(other.getJury().getGroup());
    }
}
