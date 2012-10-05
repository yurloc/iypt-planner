package org.iypt.planner.domain;

/**
 *
 * @author jlocker
 */
public class Juror {

    private String firstName;
    private String lastName;
    private final CountryCode country;
    private final JurorType type;
    private boolean chairCandidate = false;

    public Juror(CountryCode country, JurorType type) {
        this.country = country;
        this.type = type;
    }

    public Juror(String firstName, String lastName, CountryCode country, JurorType type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("(%s)", country);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get juror's country.
     *
     * @return country
     */
    public CountryCode getCountry() {
        return country;
    }

    /**
     * Get juror type.
     * @return juror type
     */
    public JurorType getType() {
        return type;
    }

    public boolean isChairCandidate() {
        return chairCandidate;
    }

    public void setChairCandidate(boolean chairCandidate) {
        this.chairCandidate = chairCandidate;
    }

}
