package org.iypt.planner.csv;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jlocker
 */
public class JudgingEvent {

    private final Fight fight;
    private final int stage;
    private final int role;
    private Map<Juror, Integer> marks = new HashMap<>();
    private float total;

    public JudgingEvent(Fight fight, int stage, int role) {
        this.fight = fight;
        this.stage = stage;
        this.role = role;
        this.total = 0;
    }

    void addMark(Juror juror, int mark) {
        marks.put(juror, mark);
        total += mark;
    }

    void calculate() {
        for (Juror juror : marks.keySet()) {
            juror.addBias(new Bias(juror, this, getBias(juror)));
        }
    }

    public int getMark(Juror juror) {
        return marks.get(juror);
    }

    public float getOthersAverage(Juror juror) {
        int mark = getMark(juror);
        return (total - mark) / (marks.size() - 1);
    }

    public float getBias(Juror juror) {
        int mark = getMark(juror);
        return mark - getOthersAverage(juror);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("Judging event\n");
        sb.append("Fight: ").append(fight).append("\n");
        sb.append("Stage: ").append(stage).append("\n");
        sb.append("Role:  ").append(role).append("\n");
        sb.append(String.format("Mark average: %.2f%n%n", total / marks.size()));

        for (Juror juror : marks.keySet()) {
            int mark = getMark(juror);
            float avg = getOthersAverage(juror);
            sb.append(String.format("%s {mark=%d, avg=%.2f, bias=%+.2f}%n", juror.getName(), mark, avg, mark - avg));
        }
        return sb.toString();
    }
}
