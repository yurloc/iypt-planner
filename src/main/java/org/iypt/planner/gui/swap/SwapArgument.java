package org.iypt.planner.gui.swap;

import org.iypt.planner.domain.Juror;

public interface SwapArgument {

    Juror getJuror();

    void apply(Juror other);

    boolean isTarget();
}
