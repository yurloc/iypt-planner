package org.iypt.planner.domain;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRange;
import org.drools.planner.api.domain.variable.ValueRangeType;
import org.iypt.planner.solver.LockedSeatSelectionFilter;

@PlanningEntity(movableEntitySelectionFilter = LockedSeatSelectionFilter.class)
public class VotingSeat extends AbstractSeat {

    public VotingSeat(Jury jury, int position, Juror juror) {
        super(jury, position, juror);
    }

    @PlanningVariable
    @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty = "jurors")
    @Override
    public Juror getJuror() {
        return juror;
    }

    @Override
    public Seat clone() {
        return new VotingSeat(jury, position, juror);
    }
}
