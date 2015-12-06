package org.iypt.planner.opta.drools.solver;

import org.iypt.planner.opta.drools.domain.Seat;
import org.iypt.planner.opta.drools.domain.Tournament;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

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
