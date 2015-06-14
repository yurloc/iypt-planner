package org.iypt.planner.gui;

import java.util.ArrayList;
import java.util.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;

public class RoundDetails extends Container {

    private static class RoundDetailsListenerList extends ListenerList<RoundDetailsListener> implements RoundDetailsListener {

        @Override
        public void roundChanged() {
            for (RoundDetailsListener listener : this) {
                listener.roundChanged();
            }
        }

        @Override
        public void seatSelected(SeatInfo seatInfo) {
            for (RoundDetailsListener listener : this) {
                listener.seatSelected(seatInfo);
            }
        }
    }

    private final RoundDetailsListenerList listeners = new RoundDetailsListenerList();
    private RoundModel round;

    public RoundDetails() {
        setSkin(new RoundDetailsSkin());
    }

    public ListenerList<RoundDetailsListener> getListeners() {
        return listeners;
    }

    void seatSelected(SeatInfo seatInfo) {
        listeners.seatSelected(seatInfo);
    }

    public void setData(RoundModel round) {
        this.round = round;
        listeners.roundChanged();
    }

    public String getOptimalIndependentCount() {
        if (round == null) {
            return "";
        }
        return String.format("%.4f", round.getRound().getOptimalIndependentCount());
    }

    public String getMaxJurySize() {
        if (round == null) {
            return "";
        }
        return String.valueOf(round.getRound().getMaxJurySize());
    }

    public List<SeatInfo> getIdle() {
        if (round == null) {
            return new ArrayList<>();
        }
        return round.getIdle();
    }

    public List<SeatInfo> getAway() {
        if (round == null) {
            return new ArrayList<>();
        }
        return round.getAway();
    }
}
