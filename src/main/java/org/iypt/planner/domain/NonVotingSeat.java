package org.iypt.planner.domain;

import org.iypt.planner.solver.LockedSeatSelectionFilter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity(movableEntitySelectionFilter = LockedSeatSelectionFilter.class)
public class NonVotingSeat extends AbstractSeat {

    public NonVotingSeat() {
    }

    public NonVotingSeat(Jury jury, int position, Juror juror) {
        super(jury, position, juror);
    }

    @PlanningVariable(valueRangeProviderRefs = {"jurors"}, nullable = true)
    @Override
    public Juror getJuror() {
        return juror;
    }

    @Override
    public Seat clone() {
        return new NonVotingSeat(jury, position, juror);
    }
}
