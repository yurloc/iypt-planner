package org.iypt.planner.gui;

/**
 *
 * @author jlocker
 */
public interface GroupRosterListener {

    void groupRosterChanged(GroupRoster group);

    void seatSelected(GroupRoster group, SeatInfo previousSeat);

    void seatLockChanged(GroupRoster group, SeatInfo seat);
}
