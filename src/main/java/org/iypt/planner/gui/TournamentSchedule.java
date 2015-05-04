package org.iypt.planner.gui;

import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;

/**
 *
 * @author jlocker
 */
public class TournamentSchedule extends Container {

    private static final class TournamentScheduleListenerList extends ListenerList<TournamentScheduleListener> implements TournamentScheduleListener {

        @Override
        public void scheduleChanged() {
            for (TournamentScheduleListener listener : this) {
                listener.scheduleChanged();
            }
        }

        @Override
        public void roundSelected(RoundModel round) {
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
        public void roundLockRequested(RoundModel round) {
            for (TournamentScheduleListener listener : this) {
                listener.roundLockRequested(round);
            }
        }

        @Override
        public void roundLocksChanged() {
            for (TournamentScheduleListener listener : this) {
                listener.roundLocksChanged();
            }
        }
    }
    private final TournamentScheduleListenerList tournamentScheduleListeners = new TournamentScheduleListenerList();
    private ScheduleModel schedule;

    public TournamentSchedule(ScheduleModel schedule) {
        this.schedule = schedule;
        setSkin(new TournamentScheduleSkin());
    }

    public List<RoundModel> getRounds() {
        return schedule.getRounds();
    }

    public void updateSchedule(ScheduleModel schedule) {
        this.schedule = schedule;
        tournamentScheduleListeners.scheduleChanged();
        tournamentScheduleListeners.roundLocksChanged();
    }

    void roundSelected(int roundNumber) {
        if (roundNumber >= 0) {
            tournamentScheduleListeners.roundSelected(schedule.getRounds().get(roundNumber));
        }
    }

    void requestRoundLock(RoundModel round) {
        tournamentScheduleListeners.roundLockRequested(round);
    }

    void seatSelected(SeatInfo seatInfo) {
        tournamentScheduleListeners.seatSelected(seatInfo);
    }

    void lockSeat(SeatInfo seatInfo) {
        tournamentScheduleListeners.seatLocked(seatInfo);
        tournamentScheduleListeners.scheduleChanged();
    }

    void unlockSeat(SeatInfo seatInfo) {
        tournamentScheduleListeners.seatUnlocked(seatInfo);
        tournamentScheduleListeners.scheduleChanged();
    }

    public ListenerList<TournamentScheduleListener> getTournamentScheduleListeners() {
        return tournamentScheduleListeners;
    }
}
