package org.iypt.planner.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.iypt.planner.api.domain.Group;
import org.iypt.planner.api.domain.Round;
import org.iypt.planner.api.domain.Team;

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
            Team[] teamsArr = Arrays.copyOfRange(teams, next, next + caps[i]);
            Group group = new Group(null, Arrays.asList(teamsArr));
            groups[i] = group;
            next += caps[i];
        }
        return createRound(number, groups);
    }

    public static Round createRound(int number, Group... groups) {
        List<Group> groupList = new ArrayList<>();
        char name = 65;
        for (Group group : groups) {
            groupList.add(new Group(String.valueOf(name++), group.getTeams()));
        }
        return new Round(number, groupList);
    }
}
