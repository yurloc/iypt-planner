package org.iypt.planner.gui.swap;

import org.iypt.planner.domain.Juror;

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
}
