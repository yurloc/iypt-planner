package org.iypt.planner.solver;

import org.drools.planner.core.heuristic.selector.common.decorator.SelectionFilter;
import org.drools.planner.core.score.director.ScoreDirector;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public class LockedSeatSelectionFilter implements SelectionFilter<Seat> {

    @Override
    public boolean accept(ScoreDirector scoreDirector, Seat selection) {
        Tournament t = (Tournament) scoreDirector.getWorkingSolution();
        return !t.isLocked(selection);
    }
}
