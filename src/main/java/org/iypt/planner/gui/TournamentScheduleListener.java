package org.iypt.planner.gui;

/**
 *
 * @author jlocker
 */
public interface TournamentScheduleListener {

    void scheduleChanged();

    void roundSelected(RoundModel round);

    void roundLockRequested(RoundModel round);

    void roundLocksChanged();

    void seatSelected(SeatInfo seatInfo);

    void seatLocked(SeatInfo seatInfo);

    void seatUnlocked(SeatInfo seatInfo);

    class Adapter implements TournamentScheduleListener {

        @Override
        public void scheduleChanged() {
            // do nothing
        }

        @Override
        public void roundSelected(RoundModel round) {
            // do nothing
        }

        @Override
        public void roundLockRequested(RoundModel round) {
            // do nothing
        }

        @Override
        public void roundLocksChanged() {
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
