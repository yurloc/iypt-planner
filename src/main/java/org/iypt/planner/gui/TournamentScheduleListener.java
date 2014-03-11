package org.iypt.planner.gui;

import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.gui.GroupRoster.JurorRow;

/**
 *
 * @author jlocker
 */
public interface TournamentScheduleListener {

    void scheduleChanged(TournamentSchedule tournament);

    void roundSelected(Round round);

    void jurorSelected(Juror juror);

    void jurorLocked(JurorRow jurorRow);

    void jurorUnlocked(JurorRow jurorRow);

    void requestRoundLock();

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
        public void jurorSelected(Juror juror) {
            // do nothing
        }

        @Override
        public void jurorLocked(JurorRow jurorRow) {
            // do nothing
        }

        @Override
        public void jurorUnlocked(JurorRow jurorRow) {
            // do nothing
        }

        @Override
        public void requestRoundLock() {
            // do nothing
        }
    }
}
