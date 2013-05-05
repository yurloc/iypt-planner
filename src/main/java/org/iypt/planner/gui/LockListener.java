package org.iypt.planner.gui;

import org.iypt.planner.solver.TournamentSolver;

/**
 *
 * @author jlocker
 */
public interface LockListener {

    public void roundLockChanged(TournamentSolver solver);
}
