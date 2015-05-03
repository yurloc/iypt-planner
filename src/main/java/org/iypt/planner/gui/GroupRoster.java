package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;

/**
 *
 * @author jlocker
 */
public class GroupRoster extends Container {

    private static final class GroupRosterListenerList extends ListenerList<GroupRosterListener> implements GroupRosterListener {

        @Override
        public void groupRosterChanged(GroupRoster group) {
            for (GroupRosterListener listener : this) {
                listener.groupRosterChanged(group);
            }
        }

        @Override
        public void seatSelected(GroupRoster group, SeatInfo previousSeat) {
            for (GroupRosterListener listener : this) {
                listener.seatSelected(group, previousSeat);
            }
        }

        @Override
        public void seatLockChanged(GroupRoster group, SeatInfo seat) {
            for (GroupRosterListener listener : this) {
                listener.seatLockChanged(group, seat);
            }
        }
    }
    private final GroupRosterListenerList groupRosterListenerList = new GroupRosterListenerList();
    private Room room;
    private SeatInfo selectedSeat;

    boolean isLocked() {
        return room.isLocked();
    }

    public void setSelectedSeat(SeatInfo seat) {
        SeatInfo oldSeat = this.selectedSeat;
        this.selectedSeat = seat;
        groupRosterListenerList.seatSelected(this, oldSeat);
    }

    void lockIn(int rowIndex) {
        SeatInfo seat = getSeats().get(rowIndex);
        seat.lock();
        groupRosterListenerList.seatLockChanged(this, seat);
    }

    void lockOut(int rowIndex) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void unlock(int rowIndex) {
        SeatInfo seat = getSeats().get(rowIndex);
        seat.unlock();
        groupRosterListenerList.seatLockChanged(this, seat);
    }

    GroupRoster(Room room) {
        this.room = room;
        setSkin(new GroupRosterSkin());
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
        groupRosterListenerList.groupRosterChanged(this);
    }

    public ListenerList<GroupRosterListener> getGroupRosterListeners() {
        return groupRosterListenerList;
    }
}
