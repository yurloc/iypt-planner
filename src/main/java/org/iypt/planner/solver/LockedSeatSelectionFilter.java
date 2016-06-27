package org.iypt.planner.solver;

import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 *
 * @author jlocker
 */
public class LockedSeatSelectionFilter implements SelectionFilter<Seat> {

    @Override
    public boolean accept(ScoreDirector scoreDirector, Seat seat) {
        Tournament t = (Tournament) scoreDirector.getWorkingSolution();
        return !t.isLocked(seat) && (seat.isVoting() || !seat.isOccupied());
    }
}
