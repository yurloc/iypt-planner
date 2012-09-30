package org.iypt.domain;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author jlocker
 */
public class Conflict {
    
    private Set<CountryCode> countries;

    public Conflict(CountryCode c1, CountryCode c2) {
        countries = EnumSet.of(c1, c2);
    }

    public Conflict(CountryCode... countries) {
        this.countries = EnumSet.of(countries[0], Arrays.copyOfRange(countries, 1, countries.length));
    }

    public boolean contains(CountryCode c1, CountryCode c2) {
        return countries.contains(c1) && countries.contains(c2);
    }
    /**
     * Get the value of countries
     *
     * @return the value of countries
     */
    public Set<CountryCode> getCountries() {
        return countries;
    }

    /**
     * Set the value of countries
     *
     * @param countries new value of countries
     */
    public void setCountries(Set<CountryCode> countries) {
        this.countries = countries;
    }

}
