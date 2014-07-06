package org.iypt.planner.domain;

import java.util.Arrays;

/**
 *
 * @author jlocker
 */
public class RoundFactory {

    private RoundFactory() {
        // hide default constructor
    }

    /**
     * Creates a new round and initializes its groups as required by the given number of teams.
     *
     * @param number round number
     * @param teams teams that will occupy groups in the created round
     * @return the created round with initialized groups
     */
    public static Round createRound(int number, Team... teams) {
        Group[] groups = new Group[teams.length / 3];
        int[] caps = new int[groups.length];

        for (int i = 0; i < teams.length; i++) {
            caps[i % caps.length]++;
        }

        int next = 0;
        for (int i = 0; i < caps.length; i++) {
            Group group = new Group(Arrays.copyOfRange(teams, next, next + caps[i]));
            groups[i] = group;
            next += caps[i];
        }
        return createRound(number, groups);
    }

    public static Round createRound(int number, Group... groups) {
        Round r = new Round(number);
        r.addGroups(groups);
        char name = 65;
        for (Group group : groups) {
            group.setName(String.valueOf(name++));
        }
        return r;
    }

}
