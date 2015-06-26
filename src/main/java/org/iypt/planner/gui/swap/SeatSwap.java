package org.iypt.planner.gui.swap;

import org.iypt.planner.domain.Juror;
import org.iypt.planner.gui.SeatInfo;

public class SeatSwap implements SwapArgument {

    private final SeatInfo seat;

    public SeatSwap(SeatInfo seat) {
        this.seat = seat;
    }

    @Override
    public Juror getJuror() {
        return seat.getJuror();
    }

    @Override
    public void apply(Juror other) {
        seat.getSeat().setJuror(other);
    }

    @Override
    public boolean isTarget() {
        return true;
    }
}
