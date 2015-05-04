package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;

/**
 *
 * @author jlocker
 */
public class RoomView extends Container {

    private static final class RoomViewListenerList extends ListenerList<RoomViewListener> implements RoomViewListener {

        @Override
        public void roomChanged() {
            for (RoomViewListener listener : this) {
                listener.roomChanged();
            }
        }

        @Override
        public void seatSelected(RoomView room, SeatInfo previousSeat) {
            for (RoomViewListener listener : this) {
                listener.seatSelected(room, previousSeat);
            }
        }

        @Override
        public void seatLockChanged(RoomView room, SeatInfo seat) {
            for (RoomViewListener listener : this) {
                listener.seatLockChanged(room, seat);
            }
        }
    }
    private final RoomViewListenerList roomViewListenerList = new RoomViewListenerList();
    private Room room;
    private SeatInfo selectedSeat;

    boolean isLocked() {
        return room.isLocked();
    }

    public SeatInfo getSelectedSeat() {
        return selectedSeat;
    }

    public void setSelectedSeat(SeatInfo seat) {
        SeatInfo oldSeat = this.selectedSeat;
        this.selectedSeat = seat;
        roomViewListenerList.seatSelected(this, oldSeat);
    }

    void lockIn(int rowIndex) {
        SeatInfo seat = getSeats().get(rowIndex);
        seat.lock();
        roomViewListenerList.seatLockChanged(this, seat);
    }

    void lockOut(int rowIndex) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void unlock(int rowIndex) {
        SeatInfo seat = getSeats().get(rowIndex);
        seat.unlock();
        roomViewListenerList.seatLockChanged(this, seat);
    }

    RoomView(Room room) {
        this.room = room;
        setSkin(new RoomViewSkin());
    }

    public String getGroupName() {
        return room.getGroupName();
    }

    public List<CountryCode> getTeams() {
        return room.getTeams();
    }

    public List<SeatInfo> getSeats() {
        return room.getSeats();
    }

    void update(Room room) {
        this.room = room;
        roomViewListenerList.roomChanged();
    }

    public ListenerList<RoomViewListener> getRoomViewListenerList() {
        return roomViewListenerList;
    }
}
