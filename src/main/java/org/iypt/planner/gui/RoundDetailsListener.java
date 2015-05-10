package org.iypt.planner.gui;

public interface RoundDetailsListener {

    void roundChanged();

    void seatSelected(SeatInfo seatInfo);

    public class Adapter implements RoundDetailsListener {

        @Override
        public void roundChanged() {
            // do nothing
        }

        @Override
        public void seatSelected(SeatInfo seatInfo) {
            // do nothing
        }
    }
}
