package org.iypt.planner.domain;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRange;
import org.drools.planner.api.domain.variable.ValueRangeType;

/**
 *
 * @author jlocker
 */
@PlanningEntity
public class JurySeat {

    // fixed
    private final Jury jury;
    private int position;
    // planning variable
    private Juror juror;

    public JurySeat(Jury jury, int position, Juror juror) {
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
        return String.format("[%s]-[%s]", jury, juror);
    }

    public JurySeat clone() {
        return new JurySeat(jury, position, juror);
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
