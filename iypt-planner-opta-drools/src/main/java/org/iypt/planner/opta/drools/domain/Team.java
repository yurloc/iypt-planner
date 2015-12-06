package org.iypt.planner.opta.drools.domain;

import com.neovisionaries.i18n.CountryCode;

/**
 *
 * @author jlocker
 */
public class Team {

    private final CountryCode country;

    public Team(CountryCode country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return String.format("Team (%s)", country);
    }

    /**
     * Get the country this team is representing.
     *
     * @return the value of country
     */
    public CountryCode getCountry() {
        return country;
    }
}
