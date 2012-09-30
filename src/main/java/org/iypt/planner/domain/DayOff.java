package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class DayOff {
    private Juror juror;
    private int day;

    public DayOff(Juror juror, int day) {
        this.juror = juror;
        this.day = day;
    }

    public Juror getJuror() {
        return juror;
    }

    public void setJuror(Juror juror) {
        this.juror = juror;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

}
