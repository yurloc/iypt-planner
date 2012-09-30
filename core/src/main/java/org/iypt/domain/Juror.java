package org.iypt.domain;

/**
 *
 * @author jlocker
 */
public class Juror {

    private final String id;
    private final CountryCode country;

    public Juror(CountryCode country) {
        this.id = null;
        this.country = country;
    }

    public Juror(String id, CountryCode country) {
        this.id = id;
        this.country = country;
    }

    @Override
    public String toString() {
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
    public CountryCode getCountry() {
        return country;
    }

}
