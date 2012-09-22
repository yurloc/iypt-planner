package org.iypt.domain;

/**
 *
 * @author jlocker
 */
public class JuryMembership {
    
    private Juror juror;
    private Jury jury;

    public JuryMembership() {
    }

    public JuryMembership(Juror juror, Jury jury) {
        this.juror = juror;
        this.jury = jury;
    }

    /**
     * Get the value of jury
     *
     * @return the value of jury
     */
    public Jury getJury() {
        return jury;
    }

    /**
     * Set the value of jury
     *
     * @param jury new value of jury
     */
    public void setJury(Jury jury) {
        this.jury = jury;
    }

    /**
     * Get the value of juror
     *
     * @return the value of juror
     */
    public Juror getJuror() {
        return juror;
    }

    /**
     * Set the value of juror
     *
     * @param juror new value of juror
     */
    public void setJuror(Juror juror) {
        this.juror = juror;
    }

}
