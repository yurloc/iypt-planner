package org.iypt.planner.api.domain;

import com.neovisionaries.i18n.CountryCode;

public class Team {

    private final CountryCode country;

    public Team(CountryCode country) {
        this.country = country;
    }

    public CountryCode getCountry() {
        return country;
    }
}
