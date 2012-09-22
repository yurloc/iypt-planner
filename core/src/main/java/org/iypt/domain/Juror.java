package org.iypt.domain;

/**
 *
 * @author jlocker
 */
public class Juror {

    private final String id;
    private final Country country;

    public Juror(Country country) {
        this.id = null;
        this.country = country;
    }

    public Juror(String id, Country country) {
        this.id = id;
        this.country = country;
    }

    @Override
    public String toString() {
//        return String.format("%s (%s)", id, country.getCode());
        return String.format("(%s)", country);
    }

    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the value of country
     *
     * @return the value of country
     */
    public Country getCountry() {
        return country;
    }

}
