package org.iypt.planner.gui;

import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.solver.TournamentSolver;

/**
 *
 * @author jlocker
 */
class JurorDetails extends Container {

    private final JurorDetailsSkin skin;
    private TournamentSolver solver;
    private JurorInfo jurorInfo;
    private PlannerWindow listener;

    JurorDetails() {
        skin = new JurorDetailsSkin();
        super.setSkin(skin);
    }

    void setListener(PlannerWindow listener) {
        this.listener = listener;
    }

    void setSolver(TournamentSolver solver) {
        this.solver = solver;
    }

    void showJuror(Juror juror) {
        this.jurorInfo = solver.getJurorInfo(juror);
        skin.showJuror(jurorInfo);
    }

    void changeStatus(JurorAssignment assignment, int statusId) {
        JurorAssignment.Status newStatus = JurorAssignment.Status.values()[statusId];
        assignment.change(newStatus);
        skin.renderSchedule(jurorInfo);
    }

    void saveChanges() {
        // TODO review and improve this
        solver.applyChanges(jurorInfo);
        jurorInfo = solver.getJurorInfo(jurorInfo.getJuror());
        skin.showJuror(jurorInfo);
        listener.solutionChanged();
    }

    void revertSchedule() {
        for (JurorAssignment assignment : jurorInfo.getSchedule()) {
            assignment.reset();
        }
        skin.renderSchedule(jurorInfo);
    }
}
