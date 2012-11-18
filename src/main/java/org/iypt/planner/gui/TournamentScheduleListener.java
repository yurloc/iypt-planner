package org.iypt.planner.gui;

import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.gui.GroupRoster.JurorRow;

/**
 *
 * @author jlocker
 */
public interface TournamentScheduleListener {

    public void scheduleChanged(TournamentSchedule tournament);

    public void roundSelected(Round round);

    public void jurorSelected(Juror juror);

    public void jurorLocked(JurorRow jurorRow);

    public void jurorUnlocked(JurorRow jurorRow);

    public class Adapter implements TournamentScheduleListener {

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
    }
}
