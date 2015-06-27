package org.iypt.planner.domain;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRange;
import org.drools.planner.api.domain.variable.ValueRangeType;
import org.iypt.planner.solver.LockedSeatSelectionFilter;

@PlanningEntity(movableEntitySelectionFilter = LockedSeatSelectionFilter.class)
public class NonVotingSeat extends AbstractSeat {

    public NonVotingSeat(Jury jury, int position, Juror juror) {
        super(jury, position, juror);
    }

    @PlanningVariable(nullable = true)
    @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty = "jurors")
    @Override
    public Juror getJuror() {
        return juror;
    }

    @Override
    public Seat clone() {
        return new NonVotingSeat(jury, position, juror);
    }
}
