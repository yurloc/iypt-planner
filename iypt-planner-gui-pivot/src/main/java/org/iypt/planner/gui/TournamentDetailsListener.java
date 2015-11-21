package org.iypt.planner.gui;

public interface TournamentDetailsListener {

    void tournamentChanged();

    public class Adapter implements TournamentDetailsListener {

        @Override
        public void tournamentChanged() {
            // do-nothing
        }
    }
}
