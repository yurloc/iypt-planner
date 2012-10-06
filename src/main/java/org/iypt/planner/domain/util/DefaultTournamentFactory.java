package org.iypt.planner.domain.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public class DefaultTournamentFactory {
    
    private List<Juror> jurors = new ArrayList<>();
    private List<Round> rounds = new ArrayList<>();

    /**
     * Creates a new round and initializes its groups as required by the given number of teams. Day index of the round will be
     * equal to its number.
     * @param number
     * @param teams
     * @return the created round with initialized groups
     */
    public Round createRound(int number, Team... teams) {
        Round r = new Round(number, number);
        
        int[] caps = new int[teams.length / 3];
        
        for (int i = 0; i < teams.length; i++) {
            caps[i % caps.length]++;
        }
        
        int next = 0;
        for (int i = 0; i < caps.length; i++) {
            Group group = r.createGroup(String.valueOf((char) (65 + i)));
            group.addTeams(Arrays.copyOfRange(teams, next, next + caps[i]));
            next += caps[i];
        }
        rounds.add(r);
        return r;
    }

    public Round createRound(int number, Group... groups) {
        // FIXME remove duplicate code
        Round r = new Round(number, number);
        r.addGroups(groups);
        char name = 65;
        for (Group group : groups) {
            group.setName(String.valueOf(name++));
        }
        rounds.add(r);
        return r;
    }
    
    public void addJurors(Juror... jurors) {
        Collections.addAll(this.jurors, jurors);
    }
    
    public Tournament newTournament() {
        Tournament tournament = new Tournament();
        tournament.setJurors(jurors);
        tournament.setRounds(rounds);
        return tournament;
    }

}
