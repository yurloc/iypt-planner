package org.iypt.planner.domain;

import java.util.Objects;

public abstract class AbstractSeat implements Seat {

    // fixed
    protected Jury jury;
    protected int position;
    // planning variable
    protected Juror juror;

    public AbstractSeat() {
    }

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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(this.jury);
        hash = 73 * hash + this.position;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractSeat other = (AbstractSeat) obj;
        if (this.position != other.position) {
            return false;
        }
        if (!Objects.equals(this.jury, other.jury)) {
            return false;
        }
        return true;
    }
}
