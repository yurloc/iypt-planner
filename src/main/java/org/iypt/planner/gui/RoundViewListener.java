package org.iypt.planner.gui;

/**
 *
 * @author jlocker
 */
public interface RoundViewListener {

    void roundChanged(RoundView round);

    void seatSelected(SeatInfo seat);

    void seatLockChanged(SeatInfo seat);
}
