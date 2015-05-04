package org.iypt.planner.gui;

/**
 *
 * @author jlocker
 */
public interface RoomViewListener {

    void roomChanged();

    void seatSelected(RoomView room, SeatInfo previousSeat);

    void seatLockChanged(RoomView room, SeatInfo seat);
}
