package org.iypt.planner.api.domain;

public enum JurorType {

    INDEPENDENT,
    TEAM_LEADER;

    public static JurorType getByLetter(char letter) {
        switch (letter) {
            case 'I':
            case 'i':
                return INDEPENDENT;
            case 'T':
            case 't':
                return TEAM_LEADER;
            default:
                throw new IllegalArgumentException("No JurorType for letter: " + letter);
        }
    }
}
