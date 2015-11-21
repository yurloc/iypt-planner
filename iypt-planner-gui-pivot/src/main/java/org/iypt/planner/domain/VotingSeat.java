package org.iypt.planner.domain;

import org.iypt.planner.solver.LockedSeatSelectionFilter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity(movableEntitySelectionFilter = LockedSeatSelectionFilter.class)
public class VotingSeat extends AbstractSeat {

    public VotingSeat() {
    }

    public VotingSeat(Jury jury, int position, Juror juror) {
        super(jury, position, juror);
    }

    @Override
    public boolean isVoting() {
        return true;
    }

    @PlanningVariable(valueRangeProviderRefs = {"jurors"})
    @Override
    public Juror getJuror() {
        return juror;
    }

    @Override
    public Seat clone() {
        return new VotingSeat(jury, position, juror);
    }
}
