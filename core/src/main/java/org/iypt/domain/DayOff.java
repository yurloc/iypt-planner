package org.iypt.domain;

/**
 *
 * @author jlocker
 */
public class DayOff {
    private Juror juror;
    private int dayIndex;

    public DayOff(Juror juror, int dayIndex) {
        this.juror = juror;
        this.dayIndex = dayIndex;
    }

    public Juror getJuror() {
        return juror;
    }

    public void setJuror(Juror juror) {
        this.juror = juror;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }
    
}
