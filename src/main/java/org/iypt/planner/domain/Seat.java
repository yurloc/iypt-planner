package org.iypt.planner.domain;

public interface Seat extends Comparable<Seat> {

    Juror getJuror();

    /**
     * Get the jury this seat belongs to.
     *
     * @return the jury this seat belongs to
     */
    Jury getJury();

    /**
     * Get the exact position of this seat in the jury.
     *
     * @return number of the seat
     */
    int getPosition();

    /**
     * Determine if this is the jury chair's seat.
     *
     * @return True if this seat must be occupied by a jury chair
     */
    boolean isChair();

    /**
     * Determine if a juror is assigned to this seat.
     *
     * @return True if a juror is assigned to this seat.
     */
    boolean isOccupied();

    /**
     * Assign a juror to this seat.
     *
     * @param juror juror that will occupy this seat
     */
    void setJuror(Juror juror);

    Seat clone();
}
