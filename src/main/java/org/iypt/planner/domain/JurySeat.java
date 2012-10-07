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
    private final boolean chair;
    private final Jury jury;
    // planning variable
    private Juror juror;

    public JurySeat(boolean chair, Jury jury, Juror juror) {
        this.chair = chair;
        this.jury = jury;
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
        return new JurySeat(chair, jury, juror);
    }

    //=========================================================================================================================
    // Getters & Setters
    //=========================================================================================================================
    public boolean isChair() {
        return chair;
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
