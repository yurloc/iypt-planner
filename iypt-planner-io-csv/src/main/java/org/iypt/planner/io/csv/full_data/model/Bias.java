package org.iypt.planner.io.csv.full_data.model;

/**
 *
 * @author jlocker
 */
public class Bias {

    private Juror juror;
    private JudgingEvent event;
    private float value;

    public Bias(Juror juror, JudgingEvent event, float value) {
        this.juror = juror;
        this.event = event;
        this.value = value;
    }

    public Juror getJuror() {
        return juror;
    }

    public JudgingEvent getEvent() {
        return event;
    }

    public float getValue() {
        return value;
    }
}
