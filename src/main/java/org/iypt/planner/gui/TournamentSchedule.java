package org.iypt.planner.gui;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.gui.GroupRoster.JurorRow;
import org.iypt.planner.solver.TournamentSolver;

/**
 *
 * @author jlocker
 */
public class TournamentSchedule extends Container {

    private static final class TournamentScheduleListenerList extends ListenerList<TournamentScheduleListener> implements TournamentScheduleListener {

        @Override
        public void scheduleChanged(TournamentSchedule tournament) {
            for (TournamentScheduleListener listener : this) {
                listener.scheduleChanged(tournament);
            }
        }

        @Override
        public void roundSelected(Round round) {
            for (TournamentScheduleListener listener : this) {
                listener.roundSelected(round);
            }
        }

        @Override
        public void jurorSelected(Juror juror) {
            for (TournamentScheduleListener listener : this) {
                listener.jurorSelected(juror);
            }
        }

        @Override
        public void jurorLocked(JurorRow jurorRow) {
            for (TournamentScheduleListener listener : this) {
                listener.jurorLocked(jurorRow);
            }
        }

        @Override
        public void jurorUnlocked(JurorRow jurorRow) {
            for (TournamentScheduleListener listener : this) {
                listener.jurorUnlocked(jurorRow);
            }
        }

        @Override
        public void requestRoundLock() {
            for (TournamentScheduleListener listener : this) {
                listener.requestRoundLock();
            }
        }
    }
    private TournamentScheduleListenerList tournamentScheduleListeners = new TournamentScheduleListenerList();
    private TournamentSolver solver;

    public TournamentSchedule(TournamentSolver solver) {
        this.solver = solver;
        setSkin(new TournamentScheduleSkin());
    }

    TournamentSolver getSolver() {
        return solver;
    }

    public void updateSchedule() {
        tournamentScheduleListeners.scheduleChanged(this);
    }

    void roundSelected(int roundNumber) {
        if (roundNumber >= 0) {
            tournamentScheduleListeners.roundSelected(solver.getRound(roundNumber));
        }
    }

    void requestRoundLock() {
        tournamentScheduleListeners.requestRoundLock();
    }

    void jurorSelected(Juror juror) {
        tournamentScheduleListeners.jurorSelected(juror);
    }

    void lockJuror(JurorRow row) {
        tournamentScheduleListeners.jurorLocked(row);
        tournamentScheduleListeners.scheduleChanged(this);
    }

    void unlockJuror(JurorRow row) {
        tournamentScheduleListeners.jurorUnlocked(row);
        tournamentScheduleListeners.scheduleChanged(this);
    }

    public ListenerList<TournamentScheduleListener> getTournamentScheduleListeners() {
        return tournamentScheduleListeners;
    }
}
