package org.iypt.planner.gui;

public interface TournamentDetailsListener {

    void capacityChanged(int capacity);

    void tournamentChanged();

    void enabledStateChanged();

    public class Adapter implements TournamentDetailsListener {

        @Override
        public void capacityChanged(int capacity) {
            // do-nothing
        }

        @Override
        public void tournamentChanged() {
            // do-nothing
        }

        @Override
        public void enabledStateChanged() {
            // do-nothing
        }
    }
}
