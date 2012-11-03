package org.iypt.planner.gui;

import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;

/**
 *
 * @author jlocker
 */
public interface TournamentScheduleListener {

    public void scheduleChanged(TournamentSchedule tournament);

    public void roundSelected(Round round);

    public void jurorSelected(Juror juror);

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
    }
}
