package org.iypt.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author jlocker
 */
public class Conflict {
    
    private Set<Country> countries;

    public Conflict() {
        this.countries = new LinkedHashSet<Country>();
    }

    public Conflict(Set<Country> countries) {
        this.countries = countries;
    }

    public Conflict(Country... countries) {
        this.countries = new LinkedHashSet<Country>(countries.length);
        Collections.addAll(this.countries, countries);
    }

    public boolean contains(Country c1, Country c2) {
        return countries.contains(c1) && countries.contains(c2);
    }
    /**
     * Get the value of countries
     *
     * @return the value of countries
     */
    public Set<Country> getCountries() {
        return countries;
    }

    /**
     * Set the value of countries
     *
     * @param countries new value of countries
     */
    public void setCountries(Set<Country> countries) {
        this.countries = countries;
    }

}
