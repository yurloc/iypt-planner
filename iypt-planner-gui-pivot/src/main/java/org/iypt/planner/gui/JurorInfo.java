package org.iypt.planner.gui;

import java.util.List;
import org.iypt.planner.opta.drools.domain.Conflict;
import org.iypt.planner.opta.drools.domain.Juror;
import org.iypt.planner.opta.drools.domain.JurorLoad;

/**
 * Groups all information that may be need when displaying juror details.
 *
 * @author jlocker
 */
public class JurorInfo {

    private final Juror juror;
    private final List<Conflict> conflicts;
    private final List<JurorAssignment> schedule;
    private final JurorLoad load;

    public JurorInfo(Juror juror, List<Conflict> conflicts, List<JurorAssignment> schedule, JurorLoad load) {
        this.juror = juror;
        this.conflicts = conflicts;
        this.schedule = schedule;
        this.load = load;
    }

    public Juror getJuror() {
        return juror;
    }

    public List<Conflict> getConflicts() {
        return conflicts;
    }

    public List<JurorAssignment> getSchedule() {
        return schedule;
    }

    public JurorLoad getLoad() {
        return load;
    }
}
