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
public class JuryMembership {

    private Jury jury; // fixed
    private Juror juror; // planning variable

    public JuryMembership() {
    }

    public JuryMembership(Jury jury, Juror juror) {
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
        return String.format("[%s]-[%s]", jury, juror == null ? null : juror.compactName());
    }
    
    public JuryMembership clone() {
        JuryMembership clone = new JuryMembership();
        clone.jury = jury;
        clone.juror = juror;
        return clone;
    }

    //=========================================================================================================================
    // Getters & Setters
    //=========================================================================================================================
    
    /**
     * Get the value of jury
     *
     * @return the value of jury
     */
    public Jury getJury() {
        return jury;
    }

    /**
     * Set the value of jury
     *
     * @param jury new value of jury
     */
    public void setJury(Jury jury) {
        this.jury = jury;
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
