package org.iypt.planner.gui.swap;

import java.util.Objects;
import org.iypt.planner.opta.drools.domain.Juror;

public class IdleSwap implements SwapArgument {

    private final Juror juror;

    public IdleSwap(Juror juror) {
        this.juror = juror;
    }

    @Override
    public Juror getJuror() {
        return juror;
    }

    @Override
    public void apply(Juror other) {
        // do nothing
    }

    @Override
    public boolean isTarget() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.juror);
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
        final IdleSwap other = (IdleSwap) obj;
        if (!Objects.equals(this.juror, other.juror)) {
            return false;
        }
        return true;
    }
}
