package org.iypt.planner.gui;

/**
 *
 * @author jlocker
 */
public interface RoundViewListener {

    void roundChanged();

    void seatSelected(SeatInfo seat);

    void seatLockChanged(SeatInfo seat);
}
