package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class Conflict {

    private final Juror juror;
    private final CountryCode country;

    public Conflict(Juror juror, CountryCode country) {
        this.juror = juror;
        this.country = country;
    }

    public CountryCode getCountry() {
        return country;
    }

    public Juror getJuror() {
        return juror;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", juror, country);
    }

}
