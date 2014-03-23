package org.iypt.planner.gui;

import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public interface TournamentScheduleListener {

    void scheduleChanged(TournamentSchedule tournament);

    void roundSelected(Round round);

    void roundLockRequested(Round round);

    void roundLocksChanged(Tournament tournament);

    void seatSelected(SeatInfo seatInfo);

    void seatLocked(SeatInfo seatInfo);

    void seatUnlocked(SeatInfo seatInfo);

    class Adapter implements TournamentScheduleListener {

        @Override
        public void scheduleChanged(TournamentSchedule tournament) {
            // do nothing
        }

        @Override
        public void roundSelected(Round round) {
            // do nothing
        }

        @Override
        public void roundLockRequested(Round round) {
            // do nothing
        }

        @Override
        public void roundLocksChanged(Tournament tournament) {
            // do nothing
        }

        @Override
        public void seatSelected(SeatInfo seatInfo) {
            // do nothing
        }

        @Override
        public void seatLocked(SeatInfo seatInfo) {
            // do nothing
        }

        @Override
        public void seatUnlocked(SeatInfo seatInfo) {
            // do nothing
        }
    }
}
