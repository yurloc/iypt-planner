package org.iypt.planner.domain;

import com.neovisionaries.i18n.CountryCode;

/**
 *
 * @author jlocker
 */
public class Juror {

    public static final Juror NULL = new Juror(null, null, null, null, false);
    private String firstName;
    private String lastName;
    private String compactName;
    private final CountryCode country;
    private final JurorType type;
    private boolean chairCandidate = false;
    private boolean experienced = true;
    private int firstAvailable = 1;
    private double bias = 0;

    public Juror(CountryCode country, JurorType type) {
        this.country = country;
        this.type = type;
        setCompactName();
    }

    public Juror(String firstName, String lastName, CountryCode country, JurorType type, boolean chairCandidate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.type = type;
        this.chairCandidate = chairCandidate;
        setCompactName();
    }

    public Juror(String firstName, String lastName, CountryCode country, JurorType type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.type = type;
        setCompactName();
    }

    private void setCompactName() {
        compactName = String.format("%s%c%c",
                country == null ? "--" : country,
                firstName == null ? '_' : firstName.toLowerCase().charAt(0),
                lastName == null ? '_' : lastName.toLowerCase().charAt(0));
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return compactName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        setCompactName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        setCompactName();
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
     *
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

    public boolean isExperienced() {
        return experienced;
    }

    public void setExperienced(boolean experienced) {
        this.experienced = experienced;
    }

    public int getFirstAvailable() {
        return firstAvailable;
    }

    public void setFirstAvailable(int firstAvailable) {
        this.firstAvailable = firstAvailable;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }
}
