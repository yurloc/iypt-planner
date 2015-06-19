package org.iypt.planner.domain;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRange;
import org.drools.planner.api.domain.variable.ValueRangeType;
import org.iypt.planner.solver.LockedSeatSelectionFilter;

/**
 *
 * @author jlocker
 */
@PlanningEntity(movableEntitySelectionFilter = LockedSeatSelectionFilter.class)
public class Seat implements Comparable<Seat> {

    // fixed
    private final Jury jury;
    private final int position;
    // planning variable
    private Juror juror;

    public Seat(Jury jury, int position, Juror juror) {
        this.jury = jury;
        this.position = position;
        this.juror = juror;
    }

    /**
     * Get the jury this seat belongs to.
     *
     * @return the jury this seat belongs to
     */
    public Jury getJury() {
        return jury;
    }

    /**
     * Get the exact position of this seat in the jury.
     *
     * @return number of the seat
     */
    public int getPosition() {
        return position;
    }

    @PlanningVariable
    @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty = "jurors")
    public Juror getJuror() {
        return juror;
    }

    /**
     * Assign a juror to this seat.
     *
     * @param juror juror that will occupy this seat
     */
    public void setJuror(Juror juror) {
        this.juror = juror;
    }

    public Seat clone() {
        return new Seat(jury, position, juror);
    }

    /**
     * Determine if a juror is assigned to this seat.
     *
     * @return True if a juror is assigned to this seat.
     */
    public boolean isOccupied() {
        return juror != null && juror != Juror.NULL;
    }

    /**
     * Determine if this is the jury chair's seat.
     *
     * @return True if this seat must be occupied by a jury chair
     */
    public boolean isChair() {
        return position == 0;
    }

    @Override
    public String toString() {
        return String.format("[Seat %s:%d]-[%s]", jury.coords(), position + 1, juror);
    }

    @Override
    public int compareTo(Seat other) {
        if (jury.equals(other.jury)) {
            return position - other.position;
        }
        return jury.getGroup().compareTo(other.jury.getGroup());
    }
}
