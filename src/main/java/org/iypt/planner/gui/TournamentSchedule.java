package org.iypt.planner.gui;

import java.util.ArrayList;
import java.util.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
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
        public void seatSelected(SeatInfo seatInfo) {
            for (TournamentScheduleListener listener : this) {
                listener.seatSelected(seatInfo);
            }
        }

        @Override
        public void seatLocked(SeatInfo seatInfo) {
            for (TournamentScheduleListener listener : this) {
                listener.seatLocked(seatInfo);
            }
        }

        @Override
        public void seatUnlocked(SeatInfo seatInfo) {
            for (TournamentScheduleListener listener : this) {
                listener.seatUnlocked(seatInfo);
            }
        }

        @Override
        public void roundLockRequested(Round round) {
            for (TournamentScheduleListener listener : this) {
                listener.roundLockRequested(round);
            }
        }

        @Override
        public void roundLocksChanged(Tournament tournament) {
            for (TournamentScheduleListener listener : this) {
                listener.roundLocksChanged(tournament);
            }
        }
    }
    private final TournamentScheduleListenerList tournamentScheduleListeners = new TournamentScheduleListenerList();
    private final TournamentSolver solver;

    public TournamentSchedule(TournamentSolver solver) {
        this.solver = solver;
        setSkin(new TournamentScheduleSkin());
    }

    List<SeatInfo> getSeats(Group group) {
        ArrayList<SeatInfo> seats = new ArrayList<>();
        for (Seat seat : solver.getTournament().getSeats(group.getJury())) {
            SeatInfo seatInfo = SeatInfo.newInstance(seat);
            if (solver.getTournament().isLocked(seat)) {
                seatInfo.lock();
            }
            seats.add(seatInfo);
        }
        return seats;
    }

    Tournament getTournament() {
        return solver.getTournament();
    }

    public void updateSchedule() {
        tournamentScheduleListeners.scheduleChanged(this);
        tournamentScheduleListeners.roundLocksChanged(getTournament());
    }

    void roundSelected(int roundNumber) {
        if (roundNumber >= 0) {
            tournamentScheduleListeners.roundSelected(solver.getRound(roundNumber));
        }
    }

    void requestRoundLock(Round round) {
        tournamentScheduleListeners.roundLockRequested(round);
    }

    void seatSelected(SeatInfo seatInfo) {
        tournamentScheduleListeners.seatSelected(seatInfo);
    }

    void lockSeat(SeatInfo seatInfo) {
        tournamentScheduleListeners.seatLocked(seatInfo);
        tournamentScheduleListeners.scheduleChanged(this);
    }

    void unlockSeat(SeatInfo seatInfo) {
        tournamentScheduleListeners.seatUnlocked(seatInfo);
        tournamentScheduleListeners.scheduleChanged(this);
    }

    public ListenerList<TournamentScheduleListener> getTournamentScheduleListeners() {
        return tournamentScheduleListeners;
    }
}
