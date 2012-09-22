package org.iypt.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRange;
import org.drools.planner.api.domain.variable.ValueRangeType;

/**
 *
 * @author jlocker
 */
@PlanningEntity
public class Jury {

    private int capacity;
    private Juror chair;
    private Set<Juror> members;

    public Jury() {
        members = new LinkedHashSet<Juror>();
    }

    /**
     * Get the value of members
     *
     * @return the value of members
     */
    @PlanningVariable
    @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty = "jurors")
    public Set<Juror> getMembers() {
        return members;
    }

    public Jury clone() {
        Jury clone = new Jury();
        clone.capacity = capacity;
        clone.chair = chair;
        for (Juror j : members) {
            clone.members.add(j);
        }
        return clone;
    }
    
    //=========================================================================================================================
    // Getters & Setters
    //=========================================================================================================================

    /**
     * Get the value of capacity
     *
     * @return the value of capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Set the value of capacity
     *
     * @param capacity new value of capacity
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Set the value of members
     *
     * @param members new value of members
     */
    public void setMembers(Set<Juror> members) {
        this.members = members;
    }

    /**
     * Get the value of chair
     *
     * @return the value of chair
     */
    public Juror getChair() {
        return chair;
    }

    /**
     * Set the value of chair
     *
     * @param chair new value of chair
     */
    public void setChair(Juror chair) {
        this.chair = chair;
    }

}
