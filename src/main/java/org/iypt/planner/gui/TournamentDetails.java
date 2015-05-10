package org.iypt.planner.gui;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Tournament;

public class TournamentDetails extends Container {

    private class TournamentDetailsListenerList extends ListenerList<TournamentDetailsListener> implements TournamentDetailsListener {

        @Override
        public void capacityChanged(int capacity) {
            for (TournamentDetailsListener listener : this) {
                listener.capacityChanged(capacity);
            }
        }

        @Override
        public void tournamentChanged() {
            for (TournamentDetailsListener listener : this) {
                listener.tournamentChanged();
            }
        }

        @Override
        public void enabledStateChanged() {
            for (TournamentDetailsListener listener : this) {
                listener.enabledStateChanged();
            }
        }
    }

    private final TournamentDetailsListenerList listeners = new TournamentDetailsListenerList();
    private Tournament tournament;

    public TournamentDetails() {
        setSkin(new TournamentDetailsSkin());
    }

    public ListenerList<TournamentDetailsListener> getListeners() {
        return listeners;
    }

    void setCapacity(int capacity) {
        listeners.capacityChanged(capacity);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        listeners.enabledStateChanged();
    }

    public void setData(Tournament tournament) {
        this.tournament = tournament;
        listeners.tournamentChanged();
    }

    public String getTotalJurors() {
        if (tournament == null) {
            return "";
        }
        return Integer.toString(tournament.getJurors().size());
    }

    public int getJuryCapacity() {
        if (tournament == null) {
            return 1;
        }
        return tournament.getJuryCapacity();
    }

    public String getTotalSeats() {
        if (tournament == null) {
            return "";
        }
        return Integer.toString(tournament.getSeats().size());
    }

    public String getTotalMandays() {
        if (tournament == null) {
            return "";
        }
        return Integer.toString(tournament.getJurors().size() * tournament.getRounds().size() - tournament.getAbsences().size());
    }

    public String getOptimalLoad() {
        if (tournament == null) {
            return "";
        }
        return String.format("%.4f", tournament.getStatistics().getOptimalLoad());
    }
}
