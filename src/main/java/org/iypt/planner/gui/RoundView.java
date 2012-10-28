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
    private Tournament tournament;
    private Round round;

    public RoundView(Tournament tournament, Round round) {
        this.round = round;
        this.tournament = tournament;
        setSkin(new RoundViewSkin());
    }

    public void update(Tournament tournament, Round round) {
        this.round = round;
        this.tournament = tournament;
        roundViewListeners.scheduleChanged(this);
    }

    public Round getRound() {
        return round;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public ListenerList<RoundViewListener> getRoundViewListeners() {
        return roundViewListeners;
    }
}
