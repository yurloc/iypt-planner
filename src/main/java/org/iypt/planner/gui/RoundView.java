package org.iypt.planner.gui;

import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;

/**
 *
 * @author jlocker
 */
public class RoundView extends Container {

    private static final class RoundViewListenerList extends ListenerList<RoundViewListener> implements RoundViewListener {

        @Override
        public void roundChanged(RoundView round) {
            for (RoundViewListener listener : this) {
                listener.roundChanged(round);
            }
        }

        @Override
        public void seatSelected(SeatInfo seat) {
            for (RoundViewListener listener : this) {
                listener.seatSelected(seat);
            }
        }

        @Override
        public void seatLockChanged(SeatInfo seat) {
            for (RoundViewListener listener : this) {
                listener.seatLockChanged(seat);
            }
        }
    }
    private final RoundViewListenerList roundViewListeners = new RoundViewListenerList();
    private RoundModel round;

    public RoundView(RoundModel round) {
        this.round = round;
        setSkin(new RoundViewSkin());
    }

    public void update(RoundModel round) {
        this.round = round;
        roundViewListeners.roundChanged(this);
    }

    public RoundModel getRound() {
        return round;
    }

    public List<Room> getRooms() {
        return round.getRooms();
    }

    public boolean isLocked() {
        return round.isLocked();
    }

    void seatLockChanged(SeatInfo seat) {
        roundViewListeners.seatLockChanged(seat);
    }

    void seatSelected(SeatInfo selectedSeat) {
        roundViewListeners.seatSelected(selectedSeat);
    }

    public ListenerList<RoundViewListener> getRoundViewListeners() {
        return roundViewListeners;
    }
}
