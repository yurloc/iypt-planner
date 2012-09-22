package org.iypt.domain;

/**
 *
 * @author jlocker
 */
public class Team {

    private int id;
    private Country country;

    public Team(Country country) {
        this.country = country;
    }

    public Team(int id, Country country) {
        this.id = id;
        this.country = country;
    }

    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public int getId() {
        return id;
    }

    /**
     * Set the value of id
     *
     * @param id new value of id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the value of country
     *
     * @return the value of country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Set the value of country
     *
     * @param country new value of country
     */
    public void setCountry(Country country) {
        this.country = country;
    }
}
