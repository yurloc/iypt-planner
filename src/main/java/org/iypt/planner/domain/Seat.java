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
public class Seat {

    // fixed
    private final Jury jury;
    private int position;
    // planning variable
    private Juror juror;

    public Seat(Jury jury, int position, Juror juror) {
        this.jury = jury;
        this.position = position;
        this.juror = juror;
    }

    /**
     * Get the value of juror
     *
     * @return the value of juror
     */
    @PlanningVariable
    @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty = "jurors")
    public Juror getJuror() {
        return juror;
    }

    @Override
    public String toString() {
        return String.format("[Seat %s:%d]-[%s]", jury.coords(), position + 1, juror);
    }

    public Seat clone() {
        return new Seat(jury, position, juror);
    }

    public boolean isOccupied() {
        return juror != null && juror != Juror.NULL;
    }

    //=========================================================================================================================
    // Getters & Setters
    //=========================================================================================================================
    public boolean isChair() {
        return position == 0;
    }

    public int getPosition() {
        return position;
    }

    /**
     * Get the value of jury
     *
     * @return the value of jury
     */
    public Jury getJury() {
        return jury;
    }

    /**
     * Set the value of juror
     *
     * @param juror new value of juror
     */
    public void setJuror(Juror juror) {
        this.juror = juror;
    }
}
