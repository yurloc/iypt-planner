package org.iypt.planner.api.domain;

import java.util.List;

import com.neovisionaries.i18n.CountryCode;

public class Juror {

    private final String firstName;
    private final String lastName;
    private final List<CountryCode> conflicts;
    private final JurorType type;
    private final boolean chairCandidate;
    private final boolean experienced;
    private final List<Round> missingRounds;

    public Juror(
            String firstName,
            String lastName,
            List<CountryCode> conflicts,
            JurorType type,
            boolean chairCandidate,
            boolean experienced,
            List<Round> missingRounds) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.conflicts = conflicts;
        this.type = type;
        this.chairCandidate = chairCandidate;
        this.experienced = experienced;
        this.missingRounds = missingRounds;
    }

    public List<CountryCode> getConflicts() {
        return conflicts;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Round> getMissingRounds() {
        return missingRounds;
    }

    public JurorType getType() {
        return type;
    }

    public boolean isChairCandidate() {
        return chairCandidate;
    }

    public boolean isExperienced() {
        return experienced;
    }
}
