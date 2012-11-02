package org.iypt.planner.gui;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;

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
    }
    private TournamentScheduleListenerList tournamentScheduleListeners = new TournamentScheduleListenerList();
    private Tournament tournament;

    public TournamentSchedule(Tournament tournament) {
        this.tournament = tournament;
        setSkin(new TournamentScheduleSkin());
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void updateSchedule(Tournament tournament) {
        this.tournament = tournament;
        tournamentScheduleListeners.scheduleChanged(this);
    }

    void roundSelected(int roundNumber) {
        if (roundNumber >= 0) {
            tournamentScheduleListeners.roundSelected(tournament.getRounds().get(roundNumber));
        }
    }

    public ListenerList<TournamentScheduleListener> getTournamentScheduleListeners() {
        return tournamentScheduleListeners;
    }
}
