package org.iypt.planner.gui;

import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.solver.TournamentSolver;

/**
 *
 * @author jlocker
 */
public class JurorDetails extends Container {

    private final TournamentSolver solver;
    private final JurorDetailsSkin skin;
    private Juror juror;
    private PlannerWindow listener;

    public JurorDetails(TournamentSolver solver) {
        this.solver = solver;
        skin = new JurorDetailsSkin();
        super.setSkin(skin);
    }

    public TournamentSolver getSolver() {
        return solver;
    }

    public void showJuror(Juror juror) {
        this.juror = juror;
        skin.showJuror(juror);
    }

    public Juror getJuror() {
        return juror;
    }

    public void setListener(PlannerWindow listener) {
        this.listener = listener;
    }

    void saveChanges() {
        solver.applyChanges(juror);
        skin.showJuror(juror);
        listener.solutionChanged();
    }
}