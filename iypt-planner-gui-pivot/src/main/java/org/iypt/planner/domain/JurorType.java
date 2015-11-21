package org.iypt.planner.domain;

/**
 * There are two types of jurors: a) independent juror, b) team leader.
 *
 * @author jlocker
 */
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
