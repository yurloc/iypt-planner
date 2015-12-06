package org.iypt.planner.gui.swap;

import java.util.Objects;
import org.iypt.planner.gui.SeatInfo;
import org.iypt.planner.opta.drools.domain.Juror;

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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.seat);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SeatSwap other = (SeatSwap) obj;
        if (!Objects.equals(this.seat, other.seat)) {
            return false;
        }
        return true;
    }
}
