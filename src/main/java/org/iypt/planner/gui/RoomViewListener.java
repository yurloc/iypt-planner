package org.iypt.planner.gui;

/**
 *
 * @author jlocker
 */
public interface RoomViewListener {

    void roomChanged(RoomView room);

    void seatSelected(RoomView room, SeatInfo previousSeat);

    void seatLockChanged(RoomView room, SeatInfo seat);
}
