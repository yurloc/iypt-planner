package org.iypt.planner.gui;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public class RoundView extends Container {

    private static final class RoundViewListenerList extends ListenerList<RoundViewListener> implements RoundViewListener {

        @Override
        public void scheduleChanged(RoundView round) {
            for (RoundViewListener listener : this) {
                listener.scheduleChanged(round);
            }
        }
    }
    private RoundViewListenerList roundViewListeners = new RoundViewListenerList();
    private TournamentSchedule schedule;
    private Round round;

    public RoundView(TournamentSchedule schedule, Round round) {
        this.round = round;
        this.schedule = schedule;
        setSkin(new RoundViewSkin());
    }

    public void update(TournamentSchedule schedule, Round round) {
        this.round = round;
        this.schedule = schedule;
        roundViewListeners.scheduleChanged(this);
    }

    public Round getRound() {
        return round;
    }

    public TournamentSchedule getSchedule() {
        return schedule;
    }

    public Tournament getTournament() {
        return schedule.getTournament();
    }

    public ListenerList<RoundViewListener> getRoundViewListeners() {
        return roundViewListeners;
    }
}
